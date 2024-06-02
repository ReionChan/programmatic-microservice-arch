package io.github.reionchan.config;

import io.github.reionchan.dto.R;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import reactor.core.publisher.Mono;

import java.util.Map;

import static io.github.reionchan.util.RStatusUtil.of;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.Series.CLIENT_ERROR;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * 网关配置类
 *
 * @author Reion
 * @date 2023-06-06
 **/
@Configuration
public class GatewayConfig {
    private final ServerProperties serverProperties;

    public GatewayConfig(ServerProperties serverProperties) {
        this.serverProperties = serverProperties;
    }

    /**
     * 网关异常统一格式处理
     * 通过重写 {@code DefaultErrorWebExceptionHandler#renderErrorResponse} 方法
     */
    @Bean
    @Order(-1)
    public ErrorWebExceptionHandler errorWebExceptionHandler(ErrorAttributes errorAttributes,
                                                             WebProperties webProperties,
                                                             ObjectProvider<ViewResolver> viewResolvers,
                                                             ServerCodecConfigurer serverCodecConfigurer,
                                                             ApplicationContext applicationContext) {
        GatewayErrorWebExceptionHandler exceptionHandler = new GatewayErrorWebExceptionHandler(errorAttributes,
                webProperties.getResources(),
                this.serverProperties.getError(),
                applicationContext);
        exceptionHandler.setViewResolvers(viewResolvers.orderedStream().toList());
        exceptionHandler.setMessageWriters(serverCodecConfigurer.getWriters());
        exceptionHandler.setMessageReaders(serverCodecConfigurer.getReaders());
        return exceptionHandler;
    }

    private static class GatewayErrorWebExceptionHandler extends DefaultErrorWebExceptionHandler {
        public GatewayErrorWebExceptionHandler(ErrorAttributes errorAttributes, WebProperties.Resources resources,
                                               ErrorProperties errorProperties, ApplicationContext applicationContext) {
            super(errorAttributes, resources, errorProperties, applicationContext);
        }
        @SuppressWarnings("rawtypes")
        @Override
        public Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
            Map<String, Object> error = getErrorAttributes(request, getErrorAttributeOptions(request, MediaType.ALL));
            Integer statusCode = (Integer) error.get("status");
            HttpStatus.Series series = HttpStatus.Series.resolve(statusCode);
            BodyInserter<R, ReactiveHttpOutputMessage> bodyInserter;
            if (series == CLIENT_ERROR) {
                bodyInserter = BodyInserters.fromValue(R.fail(of(HttpStatus.valueOf(statusCode)), error.get("error").toString()));
            } else {
                bodyInserter = BodyInserters.fromValue(R.error(of(INTERNAL_SERVER_ERROR), error.get("error").toString()));
            }
            return ServerResponse.status(getHttpStatus(error))
                    .contentType(APPLICATION_JSON)
                    .body(bodyInserter);
        }
    }
}