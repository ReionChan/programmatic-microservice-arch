package io.github.reionchan.service;

import io.github.reionchan.dto.R;
import io.github.reionchan.dto.users.UserDto;
import jakarta.annotation.Nonnull;

/**
 * 用户服务层
 *
 * @author Reion
 * @date 2023-04-26
 **/
public interface IUserService {
    R<UserDto> findUserByUsername(@Nonnull String username);
}
