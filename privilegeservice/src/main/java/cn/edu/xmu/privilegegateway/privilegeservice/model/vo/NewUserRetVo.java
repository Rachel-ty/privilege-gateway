package cn.edu.xmu.privilegegateway.privilegeservice.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 *
 * @author BingShuai Liu
 * @studentId 22920192204245
 * @date 2021/12/10/21:21
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewUserRetVo {
    private Long id;
    private String userName;
    private String mobile;
    private String email;
    private String name;
    private String avatar;
    private String openId;
    private Long departId;
    private String password;
    private String idNumber;
    private String passportNumber;
    private NewUserSimpleRetVo creator;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;
    private NewUserSimpleRetVo modifier;
    private Integer level;
    private Byte sign;
}
