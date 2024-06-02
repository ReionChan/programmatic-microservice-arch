package io.github.reionchan.config;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import io.github.reionchan.exception.GlobalExceptionHandler;
import io.github.reionchan.provider.ArchPasswordAuthenticationConverter;
import io.github.reionchan.provider.ArchPasswordAuthenticationProvider;
import io.github.reionchan.util.RSAKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.token.*;
import org.springframework.security.oauth2.server.authorization.web.authentication.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.stream.Collectors;

import static io.github.reionchan.consts.Claims.ROLES;
import static io.github.reionchan.util.CommonUtil.isNotEmpty;
import static org.springframework.security.config.Customizer.withDefaults;

/**
 * 授权服务器安全配置
 *
 * @author Reion
 * @date 2024-06-03
 **/
@Slf4j
@Configuration
public class AuthorizationSecurityConfig {

    private UserDetailsService userDetailsService;
    @Autowired
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

//    @Bean
//    CorsConfigurationSource corsConfigurationSource() {
//        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        CorsConfiguration config = new CorsConfiguration();
//        config.setAllowCredentials(true); // 允许认证
//        config.addAllowedOriginPattern("*");
//        config.addAllowedHeader("*"); // 允许任何头进行跨域请求
//        config.addAllowedMethod("*"); // 允许任何方法进行跨域请求
//        source.registerCorsConfiguration("/**", config);
//        return source;
//    }

    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http)
            throws Exception {
        log.info("--- OAuth2 授权服务器的 HttpSecurity 定制化配置 ---");
        OAuth2AuthorizationServerConfigurer serverConfigurer = new OAuth2AuthorizationServerConfigurer();
        serverConfigurer.tokenEndpoint(tokenEndpoint -> tokenEndpoint.accessTokenRequestConverter(
            new DelegatingAuthenticationConverter(Arrays.asList(
                new OAuth2AuthorizationCodeAuthenticationConverter(),
                new OAuth2RefreshTokenAuthenticationConverter(),
                new OAuth2ClientCredentialsAuthenticationConverter(),
                new OAuth2DeviceCodeAuthenticationConverter(),
                new ArchPasswordAuthenticationConverter()
            ))
        )).oidc(withDefaults());

        JwtGenerator jwtGenerator = new JwtGenerator(new NimbusJwtEncoder(jwkSource()));
        OAuth2AccessTokenGenerator accessTokenGenerator = new OAuth2AccessTokenGenerator();
        OAuth2RefreshTokenGenerator refreshTokenGenerator = new OAuth2RefreshTokenGenerator();
        // 访问令牌定制
        jwtGenerator.setJwtCustomizer(oAuth2TokenCustomizer());
        DelegatingOAuth2TokenGenerator delegatingOAuth2TokenGenerator =
                new DelegatingOAuth2TokenGenerator(jwtGenerator, accessTokenGenerator, refreshTokenGenerator);

        // 汇聚所有 OAuth2 配置的端点匹配器，将其追加注册到当前 httpSecurity 的 AuthorizationManagerRequestMatcherRegistry
        RequestMatcher endpointsMatcher = serverConfigurer.getEndpointsMatcher();
        http.csrf(AbstractHttpConfigurer::disable).formLogin(AbstractHttpConfigurer::disable)
            .sessionManagement(sessionCfg -> sessionCfg.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
            .oauth2Client(withDefaults())
//            .cors(cfg -> cfg.configurationSource(corsConfigurationSource()))
            .exceptionHandling(exh -> exh
                    .accessDeniedHandler(GlobalExceptionHandler::handleAuthException)
                    .authenticationEntryPoint(GlobalExceptionHandler::handleAuthException))
            .authorizeHttpRequests((authorize) -> authorize
                    .requestMatchers(endpointsMatcher).permitAll()
                    .requestMatchers("/actuator/health", "/actuator/health/**","/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                    .anyRequest().authenticated())
            .with(serverConfigurer, cfg -> cfg.tokenGenerator(delegatingOAuth2TokenGenerator));
        http.userDetailsService(userDetailsService);
        SecurityFilterChain defaultFilterChain = http.build();
        addPasswordAuthenticationProvider(http);
        return defaultFilterChain;
    }

    /**
     * 本项目类路径下包含 arch_keystore.jks 密钥库
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = RSAKeyUtil.loadFromKeyStoreFile("arch_keystore.jks", "123456", "ArchKey");
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();

        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        Set<JWSAlgorithm> jwsAlgs = new HashSet<>();
        jwsAlgs.addAll(JWSAlgorithm.Family.RSA);
        jwsAlgs.addAll(JWSAlgorithm.Family.EC);
        jwsAlgs.addAll(JWSAlgorithm.Family.HMAC_SHA);
        ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
        JWSKeySelector<SecurityContext> jwsKeySelector =
                new JWSVerificationKeySelector<>(jwsAlgs, jwkSource);
        jwtProcessor.setJWSKeySelector(jwsKeySelector);
        // Override the default Nimbus claims set verifier as NimbusJwtDecoder handles it instead
        jwtProcessor.setJWTClaimsSetVerifier((claims, context) -> {
        });
        return new NimbusJwtDecoder(jwtProcessor);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info("--- 设置授权服务器密码加密器 ---");
        return new BCryptPasswordEncoder();
    }

    @SuppressWarnings("unchecked")
    private void addPasswordAuthenticationProvider(HttpSecurity http) {
        AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManager.class);
        OAuth2AuthorizationService authorizationService = http.getSharedObject(OAuth2AuthorizationService.class);
        OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator = http.getSharedObject(OAuth2TokenGenerator.class);


        ArchPasswordAuthenticationProvider resourceOwnerPasswordAuthenticationProvider =
                new ArchPasswordAuthenticationProvider(authenticationManager, authorizationService, tokenGenerator);

        http.authenticationProvider(resourceOwnerPasswordAuthenticationProvider);
    }

    private OAuth2TokenCustomizer<JwtEncodingContext> oAuth2TokenCustomizer() {
        return ctx -> {
            Set<String> roles = new HashSet<>();
            Collection<? extends GrantedAuthority> authorities = ctx.getPrincipal().getAuthorities();
            if (isNotEmpty(authorities)) {
                roles = authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
            }
            JwtClaimsSet.Builder claimsBuilder = ctx.get(JwtClaimsSet.Builder.class);
            assert claimsBuilder != null;
            Set<Object> roleSet = new HashSet<>(roles);
            claimsBuilder.claims(map -> map.put(ROLES, Collections.unmodifiableSet(roleSet)));
        };
    }
}
