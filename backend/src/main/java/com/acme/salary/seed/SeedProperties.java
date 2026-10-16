package com.acme.salary.seed;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Seeding is opt-in: run with {@code --app.seed.enabled=true}.
 */
@ConfigurationProperties(prefix = "app.seed")
public record SeedProperties(
        @DefaultValue("false") boolean enabled,
        @DefaultValue("10000") int count,
        @DefaultValue("42") long randomSeed) {
}
