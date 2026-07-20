package com.credx.dispatchhub.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConfigurationProperties(prefix = "dispatchhub.fare")
@Getter
@Setter
public class FareProperties {

    private BigDecimal baseFare;
    private BigDecimal perKmRate;
    private BigDecimal perMinuteRate;
    private BigDecimal surgeMultiplier;
    private BigDecimal maxSurgeMultiplier;
}
