package io.github.reionchan.rpc.feign;

import io.github.reionchan.config.FeignClientConfig;
import io.github.reionchan.dto.R;
import io.github.reionchan.dto.users.UserDto;
import io.github.reionchan.rpc.UserRpc;
import io.github.reionchan.rpc.feign.fallback.UserClientFallback;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author Reion
 * @date 2024-06-02
 **/
@FeignClient(name = "arch-users", configuration = FeignClientConfig.class, fallback = UserClientFallback.class)
public interface UserClient extends UserRpc {
    @Override
    @GetMapping("/rpc/user/{userName}")
    @Operation(summary = "根据用户名查询用户", description = "用户名")
    R<UserDto> getUserByName(@PathVariable("userName") String userName);
}
