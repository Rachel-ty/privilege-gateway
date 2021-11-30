package cn.edu.xmu.privilegegateway.privilegeservice.model.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.*;

/**
 * 新用户VO
 * @author LiangJi@3229
 * @date 2020/11/10 18:41
 */
@Data
public class NewUserVo {
    /*
    Minimum eight characters, at least one letter and one number:
    "^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{8,}$"
    Minimum eight characters, at least one letter, one number and one special character:
    "^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$"
    Minimum eight characters, at least one uppercase letter, one lowercase letter and one number:
    "^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d]{8,}$"
    Minimum eight characters, at least one uppercase letter, one lowercase letter, one number and one special character:
    "^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$"
    Minimum eight and maximum 10 characters, at least one uppercase letter, one lowercase letter, one number and one special character:
    "^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,10}$"
     */
    @Length(min=6,message = "用户名长度过短")
    @NotBlank(message = "用户名不能为空")
    private String userName;
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",message = "密码格式不正确，请包含大小写字母数字及特殊符号")
    @NotBlank(message = "请输入密码")
    private String password;
    @NotBlank(message = "名称不能为空")
    private String name;
    @Pattern(regexp="[+]?[0-9*#]+",message="手机号格式不正确")
    @NotBlank(message = "手机号不能为空")
    private String mobile;
    @Email(message = "email格式不正确")
    @NotBlank(message = "email不能为空")
    private String email;
    @NotNull(message = "部门id不能为空")
    @Min(value = -1,message = "请输入正确的部门id")
    private Long departId;
    @NotBlank(message = "身份证号不能为空")
    private String idNumber;
    @NotBlank(message = "护照号不能为空")
    private String passportNumber;
}
