package io.github.reionchan.dto;

import java.io.Serializable;

/**
 * 返回状态信息
 *
 * @author Reion
 * @date 2024-06-04
 **/
public interface RStatus extends Serializable {

    /**
     * 状态码
     */
    int getCode();

    /**
     * 状态原因短语
     */
    String getReasonPhrase();

    /**
     * 是否成功
     */
    boolean isSuccess();

    RStatus SUCCESS = new RStatus() {
        @Override
        public int getCode() {
            return 200;
        }

        @Override
        public String getReasonPhrase() {
            return "成功";
        }

        @Override
        public boolean isSuccess() {
            return true;
        }
    };
}
