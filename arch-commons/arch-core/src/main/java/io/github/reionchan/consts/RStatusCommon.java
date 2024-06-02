package io.github.reionchan.consts;

import io.github.reionchan.dto.RStatus;

/**
 * 通用状态码
 *
 * @author Reion
 * @date 2024-06-02
 **/
public enum RStatusCommon implements RStatus {

    FAIL(400, false, "失败"),
    ERROR(500, false,"错误"),
    UNKNOWN(999, false, "未知"),

    FEIGN_CALL_FAIL(1000, false, "服务调用失败");


    /**
     * 状态码
     */
    private final int code;

    /**
     * 状态提示语
     */
    private final String reasonPhrase;

    private final boolean success;

    RStatusCommon(int code, boolean success, String reasonPhrase) {
        this.code = code;
        this.success = success;
        this.reasonPhrase = reasonPhrase;
    }

    @Override
    public int getCode() {
        return this.code;
    }

    @Override
    public String getReasonPhrase() {
        return this.reasonPhrase;
    }

    @Override
    public boolean isSuccess() {
        return success;
    }
}
