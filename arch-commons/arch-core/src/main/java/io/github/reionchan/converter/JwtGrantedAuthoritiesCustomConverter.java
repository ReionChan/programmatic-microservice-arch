package io.github.reionchan.converter;

import jakarta.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.Collection;
import java.util.stream.Collectors;

import static io.github.reionchan.consts.Claims.ROLES;
import static io.github.reionchan.consts.Claims.ROLE_PREFIX;
import static io.github.reionchan.util.CommonUtil.isEmpty;
import static io.github.reionchan.util.CommonUtil.isNotEmpty;

/**
 * @author Reion
 * @date 2024-06-04
 **/
public class JwtGrantedAuthoritiesCustomConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    public Collection<GrantedAuthority> convert(@Nullable Jwt jwt) {
        if (isEmpty(jwt)) return null;
        Collection<GrantedAuthority> authorities = jwtGrantedAuthoritiesConverter.convert(jwt);
        Collection<String> roles = jwt.getClaim(ROLES);
        if (isNotEmpty(roles)) {
            authorities.addAll(roles.stream().map(role -> new SimpleGrantedAuthority(ROLE_PREFIX.concat(role))).collect(Collectors.toSet()));
        }
        return authorities;
    }

    @NotNull
    @Override
    public <U> Converter<Jwt, U> andThen(@NotNull Converter<? super Collection<GrantedAuthority>, ? extends U> after) {
        return Converter.super.andThen(after);
    }
}
