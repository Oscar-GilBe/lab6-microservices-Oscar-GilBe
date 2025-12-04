package web.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuration for Circuit Breaker pattern using Resilience4j.
 * 
 * This configuration provides default settings for all circuit breakers in the application.
 * Individual circuit breakers can be further customized in application.yml.
 * 
 * Circuit Breaker Pattern Benefits:
 * - Prevents cascading failures when downstream services fail
 * - Provides fallback responses for graceful degradation
 * - Automatically recovers when downstream services become healthy
 * - Reduces load on failing services, giving them time to recover
 */
@Configuration
public class CircuitBreakerConfiguration {

    /**
     * Provides default configuration for all circuit breakers.
     * 
     * These defaults can be overridden per circuit breaker in application.yml.
     * 
     * @return Customizer for Resilience4J circuit breaker factory
     */
    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .timeLimiterConfig(TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(3))
                        .build())
                .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
                .build());
    }
}
