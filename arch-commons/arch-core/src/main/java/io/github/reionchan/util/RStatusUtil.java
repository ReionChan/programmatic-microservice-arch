package io.github.reionchan.util;

import io.github.reionchan.dto.RStatus;
import io.github.reionchan.converter.HttpStatusRStatusConverter;
import org.springframework.http.HttpStatus;

/**
 * @author Reion
 * @date 2024-06-04
 **/
public final class RStatusUtil {
    private static final HttpStatusRStatusConverter CONVERTER = new HttpStatusRStatusConverter();
    public static RStatus of(HttpStatus status) {
        return CONVERTER.convert(status);
    }
}
