package com.credx.dispatchhub.util;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtils {

    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a").withZone(ZoneOffset.UTC);

    private DateTimeUtils() {
    }

    /** Used across trip/driver responses for human-readable timestamps in logs. */
    public static String formatForDisplay(Instant instant) {
        if (instant == null) {
            return null;
        }
        return DISPLAY_FORMATTER.format(instant);
    }

    public static Instant startOfDayUtc(Instant instant) {
        return instant.atZone(ZoneOffset.UTC).toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant();
    }

    public static Instant endOfDayUtc(Instant instant) {
        return startOfDayUtc(instant).plusSeconds(86400);
    }
}
