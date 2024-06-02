package io.github.reionchan.config;

import io.github.resilience4j.core.NamingThreadFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Resilience4J 容错机制配置
 *
 * @author Reion
 * @date 2024-06-10
 **/
@Configuration
public class Resilience4JConfig {

    private static final String RESILIENCE_THREAD_POOL_NAME = "resi4j";

    private static final int CORE_SIZE = Runtime.getRuntime().availableProcessors();

    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> customizer() {
        return resilience4JCircuitBreakerFactory -> {
            resilience4JCircuitBreakerFactory.configureExecutorService(
                new ThreadPoolExecutor(CORE_SIZE-1, CORE_SIZE, 20, TimeUnit.MILLISECONDS,
                    new ArrayBlockingQueue<>(100), new NamingThreadFactory(RESILIENCE_THREAD_POOL_NAME)));
        };
    }
}
