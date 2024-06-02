package io.github.reionchan.service.impl;

import io.github.reionchan.dao.IRoleDao;
import io.github.reionchan.dao.IUserDao;
import io.github.reionchan.dto.R;
import io.github.reionchan.dto.users.UserDto;
import io.github.reionchan.entity.Role;
import io.github.reionchan.entity.User;
import io.github.reionchan.service.IUserService;
import jakarta.annotation.Nonnull;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.reionchan.util.CommonUtil.isEmpty;
import static io.github.reionchan.util.CommonUtil.isNotEmpty;

/**
 * 用户服务实现
 *
 * @author Reion
 * @date 2024-06-02
 **/
@Data
@Slf4j
@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    private IUserDao userDao;

    @Autowired
    private IRoleDao roleDao;

    /**
     * 根据名称查询业务实体 User
     * @param username 用户名
     */
    @Cacheable(cacheNames = "UserByNameCache", key = "#username", sync = true)
    public R<UserDto> findUserByUsername(@Nonnull String username) {
        User user = userDao.findByUserName(username);
        Assert.isTrue(isNotEmpty(user), "未知用户");
        UserDto dto = new UserDto();
        BeanUtils.copyProperties(user, dto);
        String roles = user.getRoles();

        List<String> roleNames;
        if (Strings.isNotBlank(roles)) {
            Set<Long> roleIds = Arrays.stream(roles.split(",")).mapToLong(Long::parseLong).boxed().collect(Collectors.toSet());
            if (roleIds.size()>0) {
                roleNames = roleDao.findAllById(roleIds).stream().map(Role::getRoleName).collect(Collectors.toList());
                dto.setRoleNames(String.join(",", roleNames));
            }
        }
        if (isEmpty(dto.getRoleNames())) dto.setRoleNames("USER");
        return R.success(dto);
    }

}
