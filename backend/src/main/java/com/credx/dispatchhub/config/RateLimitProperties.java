package com.credx.dispatchhub.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "dispatchhub.rate-limit")
@Getter
@Setter
public class RateLimitProperties {

    private boolean enabled = true;

    /** Maximum burst of requests a single client may make before being throttled. */
    private int capacity = 10;

    /** Tokens restored per minute; the sustained allowed request rate. */
    private int refillPerMinute = 10;
}
