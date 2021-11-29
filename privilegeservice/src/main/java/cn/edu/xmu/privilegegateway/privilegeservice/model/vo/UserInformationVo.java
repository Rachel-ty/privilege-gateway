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
    @NotBlank
    private String name;
    @NotBlank
    private String avatar;
    @NotBlank
    private String idNumber;
    @NotBlank
    private String passportNumber;
}
