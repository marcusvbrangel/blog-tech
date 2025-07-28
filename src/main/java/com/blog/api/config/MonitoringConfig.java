package com.blog.api.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MonitoringConfig {

    @Bean
    public Counter postCreationCounter(MeterRegistry meterRegistry) {
        return Counter.builder("blog_api_posts_created_total")
                .description("Total number of posts created")
                .register(meterRegistry);
    }

    @Bean
    public Counter userRegistrationCounter(MeterRegistry meterRegistry) {
        return Counter.builder("blog_api_users_registered_total")
                .description("Total number of users registered")
                .register(meterRegistry);
    }

    @Bean
    public Counter commentCreationCounter(MeterRegistry meterRegistry) {
        return Counter.builder("blog_api_comments_created_total")
                .description("Total number of comments created")
                .register(meterRegistry);
    }

    @Bean
    public Counter cacheHitCounter(MeterRegistry meterRegistry) {
        return Counter.builder("blog_api_cache_hits_total")
                .description("Total number of cache hits")
                .register(meterRegistry);
    }

    @Bean
    public Counter cacheMissCounter(MeterRegistry meterRegistry) {
        return Counter.builder("blog_api_cache_misses_total")
                .description("Total number of cache misses")
                .register(meterRegistry);
    }

    @Bean
    public Timer databaseQueryTimer(MeterRegistry meterRegistry) {
        return Timer.builder("blog_api_database_query_duration")
                .description("Database query execution time")
                .register(meterRegistry);
    }

    @Bean
    public Timer redisOperationTimer(MeterRegistry meterRegistry) {
        return Timer.builder("blog_api_redis_operation_duration")
                .description("Redis operation execution time")
                .register(meterRegistry);
    }
}