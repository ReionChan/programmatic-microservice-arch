package io.github.reionchan.config;

import feign.Logger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * OpenFeign 客户端定制化配置类
 *
 * @author Reion
 * @date 2024-06-04
 **/
@Slf4j
@Configuration
@ConditionalOnMissingClass({
    "org.springframework.cloud.gateway.config.GatewayAutoConfiguration"
})
public class FeignClientConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }
}
