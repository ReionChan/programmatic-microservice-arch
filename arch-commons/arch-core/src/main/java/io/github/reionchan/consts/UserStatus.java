package io.github.reionchan.consts;

import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Optional;

import static io.github.reionchan.util.CommonUtil.isNotEmpty;

/**
 * 用户状态枚举
 *
 * @author Reion
 * @date 2023-12-15
 **/
public enum UserStatus {

    ENABLED( 0, "启用"),
    DISABLED( 1, "停用");

    private final Integer value;
    private final String name;

    UserStatus(Integer value, String name) {
        this.value = value;
        this.name = name;
    }
    public Integer getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public static UserStatus of(Integer value) {
        Assert.isTrue(isNotEmpty(value), "用户状态值不能为空或");
        Optional<UserStatus> userStatus = Arrays.stream(UserStatus.values()).filter(s -> s.getValue().equals(value)).findFirst();
        Assert.isTrue(userStatus.isPresent(), "用户状态值不合法");
        return userStatus.get();
    }
}
