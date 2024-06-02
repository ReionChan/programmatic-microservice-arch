package io.github.reionchan.rpc.feign.fallback;

import io.github.reionchan.dto.R;
import io.github.reionchan.dto.users.UserDto;
import io.github.reionchan.rpc.feign.UserClient;
import org.springframework.stereotype.Component;

import static io.github.reionchan.consts.RStatusCommon.FEIGN_CALL_FAIL;

/**
 * 用户服务 Fallback
 *
 * @author Reion
 * @date 2024-06-13
 **/
@Component
public class UserClientFallback implements UserClient {
    @Override
    public R<UserDto> getUserByName(String userName) {
        return R.fail(FEIGN_CALL_FAIL);
    }
}
