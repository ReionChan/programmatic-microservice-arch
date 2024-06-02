package io.github.reionchan.dto.users;

import io.github.reionchan.validation.Create;
import io.github.reionchan.validation.Update;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@Schema(name = "UserDto", description = "用户数据传输对象")
public class UserDto implements Serializable {

    static final long serialVersionUID = 1L;

    @NotNull(message = "id 不能为空", groups = {Update.class})
    @Positive(message = "id 必须大于 0", groups = {Update.class})
    @Schema(title = "用户ID", description = "用户唯一ID")
    private Long id;

    @NotNull(message = "用户名不能为空", groups = {Create.class})
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]{7,31}$", message = "用户名8~32位，字母开头、数字、下划线组合")
    @Schema(title = "用户名", description = "长度8~32，开头为字母、下划线，之后数字、字母、下划线",  minLength = 8, maxLength = 32)
    private String userName;

    @NotNull(message = "密码不能为空", groups = {Create.class})
    @Pattern(regexp = "^[a-zA-Z0-9_.\\-]{8,64}$", message = "密码8~64位，字母、数字、下划线、点、连字符")
    @Schema(title = "密码", description = "长度8~64",  minLength = 8, maxLength = 64)
    private String password;

    @Schema(title = "用户角色权限")
    @Pattern(regexp = "^[A-Z][A-Z0-9_,\\-]{0,127}$", message = "角色名称1~128位，大写字母开头、下划线数字英文逗号组合字符")
    private String roleNames;

    @Email(message = "邮箱格式错误")
    @Schema(title = "用户邮箱")
    private String email;

    @Schema(title = "用户邮箱")
    private String avatar;

    @Schema(title = "用户邮箱")
    private String phone;

    @Min(value = 0, message = "状态值最小值 0")
    @Max(value = 1, message = "状态值最大值 1")
    @Schema(type = "integer", title = "状态：0、null-启用 1-停用")
    private Integer status;

    @Schema(title = "创建时间", description = "格式：yyyy-MM-dd hh:mm:ss")
    private Date createTime;

    @Schema(title = "修改时间", description = "格式：yyyy-MM-dd hh:mm:ss")
    private Date updateTime;
}
