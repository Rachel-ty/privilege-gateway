package cn.edu.xmu.privilegegateway.privilegeservice.model.vo;

import io.swagger.annotations.ApiModel;
import lombok.*;

import java.time.LocalDateTime;

/**
 * @author Bingshuai Liu
 * @studentId 22920192204245
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ApiModel("用户信息视图")
public class UserRetVo {
    private Long id;
    private String userName;
    private String mobile;
    private String email;
    private String avatar;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
    private Byte state;
    private Long depart_id;
    private String idNumber;
    private String passportNumber;
    private UserSimpleRetVo creator;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;
    private UserSimpleRetVo modifier;
    private Integer sign;
}
