package io.github.reionchan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * @author Reion
 * @date 2024-06-02
 **/
@SpringBootApplication
@EnableJpaRepositories
@EnableMethodSecurity
public class UserBootstrap {
    public static void main(String[] args) {
        SpringApplication.run(UserBootstrap.class, args);
    }
}
