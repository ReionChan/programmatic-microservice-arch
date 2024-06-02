package io.github.reionchan.controller;

import io.github.reionchan.consts.Scope;
import io.github.reionchan.dto.R;
import io.github.reionchan.dto.users.UserDto;
import io.github.reionchan.rpc.UserRpc;
import io.github.reionchan.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import static io.github.reionchan.consts.Scope.SERVICE;

/**
 * @author Reion
 * @date 2024-06-02
 **/
@RestController
@Tag(name = "UserController", description = "用户控制器")
public class UserController {

    @Autowired
    private IUserService userService;
    @GetMapping("/user/{userName}")
    @Operation(summary = "根据用户名查询用户", description = "用户名")
    @PreAuthorize("hasAuthority('" + SERVICE + "')")
    public R<UserDto> getUserByName(@Validated @Length(message = "用户名长度 4 ~ 32",
            min = 4, max = 32) @PathVariable("userName") String userName) {
        return userService.findUserByUsername(userName);
    }
}
