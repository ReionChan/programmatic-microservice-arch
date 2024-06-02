package io.github.reionchan.adaptor;

import io.github.reionchan.consts.UserStatus;
import io.github.reionchan.dto.R;
import io.github.reionchan.dto.users.UserDto;
import io.github.reionchan.exception.BaseRuntimeException;
import io.github.reionchan.rpc.feign.UserClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * @author Reion
 * @date 2024-06-02
 **/
@Slf4j
@Component
public class ArchUserDetailServiceAdaptor implements UserDetailsService {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private UserClient userClient;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        R<UserDto> userResp = userClient.getUserByName(username);
        if (!userResp.isSuccess()) throw new BaseRuntimeException(userResp.getMessage());
        UserDto userDto = userResp.getData();
        boolean isActive = UserStatus.ENABLED.getValue().equals(userDto.getStatus());
        return User.withUsername(userDto.getUserName()).password(userDto.getPassword())
                .accountExpired(!isActive).accountLocked(!isActive).credentialsExpired(!isActive)
                .authorities(userDto.getRoleNames().split(",")).build();
    }
}