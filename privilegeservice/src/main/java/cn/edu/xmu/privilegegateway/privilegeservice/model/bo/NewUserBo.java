package cn.edu.xmu.privilegegateway.privilegeservice.model.bo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 *
 * @author BingShuai Liu
 * @studentId 22920192204245
 * @date 2021/11/28/19:24
 */
@Data
public class NewUserBo {
    private Long id;
    private String userName;
    private String mobile;
    private String email;
    private String name;
    private String avatar;
    private String openId;
    private Long departId;
    private String password;
    private Long creatorId;
    private LocalDateTime gmtCreate;
    private Long modifierId;
    private LocalDateTime gmtModified;
    private String idNumber;
    private String passportNumber;
    private String signature;
    private String creatorName;
    private String modifierName;
    private Integer level;
}
