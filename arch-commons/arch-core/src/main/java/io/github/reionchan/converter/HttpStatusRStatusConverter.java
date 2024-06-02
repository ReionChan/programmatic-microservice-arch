package io.github.reionchan.converter;

import io.github.reionchan.dto.RStatus;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;

import static io.github.reionchan.consts.RStatusCommon.UNKNOWN;
import static io.github.reionchan.util.CommonUtil.isEmpty;

/**
 * @author Reion
 * @date 2024-06-04
 **/
public final class HttpStatusRStatusConverter implements Converter<HttpStatus, RStatus> {
    @Override
    public RStatus convert(@NotNull HttpStatus status) {
        if (isEmpty(status)) return UNKNOWN;
        return new RStatus() {
            @Override
            public int getCode() {
                return status.value();
            }

            @Override
            public String getReasonPhrase() {
                return status.getReasonPhrase();
            }

            @Override
            public boolean isSuccess() {
                return status.isError();
            }
        };
    }
}
