package io.github.reionchan.config;

import io.github.reionchan.converter.JwtGrantedAuthoritiesCustomConverter;
import io.github.reionchan.exception.GlobalExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * 客户端安全配置
 *
 * @author Reion
 * @date 2023-06-03
 **/
@Slf4j
@Configuration
// Arch-IAM 授权认证中心自行配置
@ConditionalOnMissingClass({
    "org.springframework.security.oauth2.server.authorization.OAuth2Authorization",
    "org.springframework.cloud.gateway.config.GatewayAutoConfiguration"})
public class OAuth2ClientResourceSecurityConfiguration {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        log.info("--- OAuth2 客户端的 HttpSecurity 定制化配置 ---");
        http.csrf(AbstractHttpConfigurer::disable).formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .sessionManagement(config -> config.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 激活 OAuth2 客户端支持
                .oauth2Client(withDefaults())
                // 激活 OAuth2 资源服务 JWT 认证
                .oauth2ResourceServer(resServerConf -> resServerConf.jwt(cfg ->
                        cfg.jwtAuthenticationConverter(customJwtAuthenticationConverter()))
                    .accessDeniedHandler(GlobalExceptionHandler::handleAuthException)
                    .authenticationEntryPoint(GlobalExceptionHandler::handleAuthException))
                .exceptionHandling(exh -> exh
                    .accessDeniedHandler(GlobalExceptionHandler::handleAuthException)
                    .authenticationEntryPoint(GlobalExceptionHandler::handleAuthException))
                .authorizeHttpRequests(httpReq ->
                    httpReq.requestMatchers(HttpMethod.GET, "/actuator/health",
                        "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                    .anyRequest().authenticated());
        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter customJwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new JwtGrantedAuthoritiesCustomConverter());
        return converter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info("--- 注册一个密码加密器 BCryptPasswordEncoder ---");
        return new BCryptPasswordEncoder();
    }
}
