package com.store.cosmetic.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class DateTimeConfig {
    public static final ZoneId APP_ZONE = ZoneId.of("America/El_Salvador");

    @Bean
    public Clock clock() {
        return Clock.system(APP_ZONE);
    }
}
