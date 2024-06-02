package io.github.reionchan.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

import static io.github.reionchan.dto.RStatus.SUCCESS;

/**
 * 响应包装类
 *
 * @author Reion
 * @date 2024-06-02
 **/
@Data
@ToString
@Schema(name = "R", description = "接口响应对象")
@JsonPropertyOrder({"code", "success", "message", "data"})
public final class R<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @JsonProperty("code")
    @Schema(title = "状态码")
    private int code;

    @JsonProperty("success")
    @Schema(title = "成功标志")
    private boolean success;

    @JsonProperty("message")
    @Schema(title = "失败异常消息")
    private String message;

    @JsonProperty("data")
    @Schema(title = "响应数据")
    private T data;

    /**
     * 默认构造方法，给序列化使用
     */
    public R() {
    }

    private R(RStatus status, String message) {
        this(status.getCode(), status.isSuccess(), isEmpty(message) ? status.getReasonPhrase() : message, null);
    }

    private R(RStatus status, T data) {
        this(status.getCode(), status.isSuccess(), status.getReasonPhrase(), data);
    }

    private R(int code, boolean success, String message, T data) {
        this.code = code;
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static <T> R<T> fail(@NotNull RStatus status) {
        return fail(null, status);
    }

    public static <T> R<T> fail(@NotNull T data, @NotNull RStatus status) {
        return new R<>(status.getCode(), false, status.getReasonPhrase(), data);
    }

    public static <T> R<T> fail(@NotNull RStatus status, String message) {
        return new R<>(status.getCode(), false, isEmpty(message) ? status.getReasonPhrase() : message, null);
    }

    public static <T> R<T> error(@NotNull RStatus status) {
        return error(status, null);
    }

    public static <T> R<T> error(@NotNull RStatus status, String message) {
        return new R<>(status.getCode(), false, isEmpty(message) ? status.getReasonPhrase() : message, null);
    }

    public static <T> R<T> success(@NotNull T data) {
        return success(data, null);
    }

    public static <T> R<T> success(@NotNull T data, String message) {
        return new R<>(SUCCESS.getCode(), true, isEmpty(message) ? SUCCESS.getReasonPhrase() : message, data);
    }

    private static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}

