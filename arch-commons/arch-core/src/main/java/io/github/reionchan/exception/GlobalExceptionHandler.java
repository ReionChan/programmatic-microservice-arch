package io.github.reionchan.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.reionchan.dto.R;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.server.MethodNotAllowedException;

import java.io.IOException;

import static io.github.reionchan.util.RStatusUtil.of;
import static org.springframework.http.HttpStatus.*;

/**
 * 全局异常处理
 *
 * @author Reion
 * @date 2024-06-04
 **/
@Slf4j
@RestControllerAdvice
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class GlobalExceptionHandler {

    private static ObjectMapper objMapper;

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        objMapper = objectMapper;
    }

    /**
     * 客户端参数校验异常
     */
    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler({
        ValidationException.class,
        WebExchangeBindException.class,
        MethodArgumentNotValidException.class,
        HttpMessageNotReadableException.class,
        HttpMessageConversionException.class,
        HandlerMethodValidationException.class
    })
    @Order(1)
    public R<?> handleValidateException(Exception e) {
        String errorMessage = e.getMessage();
        if (Errors.class.isAssignableFrom(e.getClass())) {
            errorMessage = ((Errors) e).getAllErrors().get(0).getDefaultMessage();
        } else if (e instanceof HttpMessageNotReadableException he) {
            errorMessage = "格式有误：" +  he.getMessage().substring(he.getMessage().indexOf(":")+2);
        } else if (e instanceof HttpMessageConversionException he) {
            errorMessage = "类型有误：" +  he.getMessage().substring(he.getMessage().indexOf(":")+2);
        } else if (e instanceof HandlerMethodValidationException he) {
            errorMessage = "格式有误：" + he.getAllValidationResults().stream()
                    .map(res -> "[" + res.getArgument() + " - " + res.getResolvableErrors().get(0).getDefaultMessage() + "] ")
                    .reduce("", String::join);
        }
        log.error("客户端参数异常: ", getRootCause(e));
        return R.fail(of(BAD_REQUEST), errorMessage);
    }

    /**
     * 客户端请求方法异常
     */
    @SuppressWarnings("MalformedFormatString")
    @ExceptionHandler(MethodNotAllowedException.class)
    @ResponseStatus(METHOD_NOT_ALLOWED)
    @Order(2)
    public R<?> handleMethodNotSupportedException(MethodNotAllowedException e) {
        log.error("方法未支持", getRootCause(e));
        return R.fail(of(METHOD_NOT_ALLOWED), String.format("方法未支持：{}", e.getHttpMethod()));
    }

    @ExceptionHandler({AuthenticationException.class})
    @ResponseStatus(UNAUTHORIZED)
    @Order(3)
    public R<?> handleNoLoginException(Exception e) {
        log.warn("未登录: {}", getRootCause(e).getMessage());
        return R.fail(of(UNAUTHORIZED));
    }

    @ExceptionHandler({AccessDeniedException.class})
    @ResponseStatus(FORBIDDEN)
    @Order(4)
    public R<?> handleNoPermitException(Exception e) {
        log.warn("未授权: {}", e.getMessage());
        return R.fail(of(FORBIDDEN));
    }

    @ExceptionHandler({BaseRuntimeException.class})
    @ResponseStatus(BAD_REQUEST)
    @Order(5)
    public R<?> handleBusinessException(Exception e) {
        log.warn("业务异常：{}", e.getMessage());
        return R.fail(of(BAD_REQUEST), e.getMessage());
    }

    @ExceptionHandler({RuntimeException.class})
    @ResponseStatus(BAD_REQUEST)
    @Order(6)
    public R<?> handleRuntimeException(Exception e) {
        log.warn("运行异常", getRootCause(e));
        return R.fail(of(BAD_REQUEST), e.getMessage());
    }

    /**
     * 服务端兜底异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @Order(7)
    public R<?> handleInternalServerException(Exception e) {
        log.error("服务端内部异常: ", getRootCause(e));
        return R.error(of(INTERNAL_SERVER_ERROR));
    }

    private static Throwable getRootCause(Throwable e) {
        Throwable root = e;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return root;
    }

    // 处理 Spring Security 认证授权异常
    public static void handleAuthException(HttpServletRequest request, HttpServletResponse response, RuntimeException exception) throws IOException {
        Throwable root = getRootCause(exception);
        if (root instanceof AuthenticationException || root instanceof AccessDeniedException) {
            HttpStatus status = root instanceof AuthenticationException ? UNAUTHORIZED : FORBIDDEN;
            response.setStatus(status.value());
            response.setCharacterEncoding("UTF8");
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(objMapper.writeValueAsString(R.fail(of(status), root.getMessage())));
        } else {
            HttpStatus status;
            boolean shouldExpose = true;
            if (root instanceof RuntimeException) {
                status = BAD_REQUEST;
            } else {
                status = INTERNAL_SERVER_ERROR;
                shouldExpose = false;
            }
            log.error("服务端内部异常: ", root);
            response.setCharacterEncoding("UTF8");
            response.setStatus(status.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(objMapper.writeValueAsString(R.fail(of(status), shouldExpose ? root.getMessage() : null)));
        }
    }
}
