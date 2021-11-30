package cn.edu.xmu.privilegegateway.privilegeservice.model.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * Created with IntelliJ IDEA.
 *
 * @author BingShuai Liu
 * @studentId 22920192204245
 * @date 2021/11/29/0:24
 */
@Data
public class UserInformationVo {
    @NotBlank(message = "名称不能为空")
    private String name;
    @NotBlank(message="头像url不能为空")
    private String avatar;
    @NotBlank(message="身份证号不能为空")
    private String idNumber;
    @NotBlank(message = "护照号不能为空")
    private String passportNumber;
}
