package io.github.reionchan.rpc;

import io.github.reionchan.dto.users.UserDto;
import io.github.reionchan.dto.R;

/**
 * @author Reion
 * @date 2024-06-02
 **/
public interface UserRpc {
    R<UserDto> getUserByName(String userName);
}
