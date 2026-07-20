package com.credx.dispatchhub.service;

import com.credx.dispatchhub.dto.response.TripResponse;
import com.credx.dispatchhub.enums.TripStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Server-sent-events fan-out for trip status changes. Clients subscribe per
 * trip; every state transition pushes the fresh TripResponse to all
 * subscribers, replacing the need to poll GET /api/trips/{id}.
 *
 * Events are published after the surrounding transaction commits, so
 * subscribers never see a state that was rolled back. Emitters are held
 * in memory, which is fine for a single instance; multiple instances would
 * need a shared broker (e.g. Redis pub/sub) behind the same interface.
 */
@Component
public class TripEventPublisher {

    private static final long EMITTER_TIMEOUT_MS = 30L * 60 * 1000;
    private static final String EVENT_NAME = "trip-update";

    private final Map<Long, List<SseEmitter>> subscribers = new ConcurrentHashMap<>();

    /**
     * Registers a subscriber for a trip and immediately sends the current
     * snapshot so clients don't need a separate initial fetch.
     */
    public SseEmitter subscribe(Long tripId, TripResponse snapshot) {
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT_MS);
        List<SseEmitter> list = subscribers.computeIfAbsent(tripId, id -> new CopyOnWriteArrayList<>());
        list.add(emitter);

        emitter.onCompletion(() -> remove(tripId, emitter));
        emitter.onTimeout(() -> remove(tripId, emitter));
        emitter.onError(e -> remove(tripId, emitter));

        send(tripId, emitter, snapshot);
        return emitter;
    }

    /** Publishes a trip update to all subscribers once the current transaction commits. */
    public void publish(TripResponse trip) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    dispatch(trip);
                }
            });
        } else {
            dispatch(trip);
        }
    }

    private void dispatch(TripResponse trip) {
        List<SseEmitter> list = subscribers.get(trip.getId());
        if (list == null) {
            return;
        }

        boolean terminal = trip.getStatus() == TripStatus.COMPLETED || trip.getStatus() == TripStatus.CANCELLED;
        for (SseEmitter emitter : list) {
            send(trip.getId(), emitter, trip);
            if (terminal) {
                emitter.complete();
            }
        }
        if (terminal) {
            subscribers.remove(trip.getId());
        }
    }

    private void send(Long tripId, SseEmitter emitter, TripResponse trip) {
        try {
            emitter.send(SseEmitter.event().name(EVENT_NAME).data(trip));
        } catch (IOException | IllegalStateException e) {
            // Client went away; drop it.
            remove(tripId, emitter);
        }
    }

    private void remove(Long tripId, SseEmitter emitter) {
        List<SseEmitter> list = subscribers.get(tripId);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) {
                subscribers.remove(tripId, list);
            }
        }
    }
}
