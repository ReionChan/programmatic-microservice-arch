package io.github.reionchan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * @author Reion
 * @date 2024-06-01
 **/
@SpringBootApplication
@EnableMethodSecurity
public class AppBootstrap {
    public static void main(String[] args) {
        SpringApplication.run(AppBootstrap.class, args);
    }
}
