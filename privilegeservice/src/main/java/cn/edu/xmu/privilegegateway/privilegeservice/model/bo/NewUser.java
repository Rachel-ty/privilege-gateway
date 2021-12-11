package cn.edu.xmu.privilegegateway.privilegeservice.model.bo;

import cn.edu.xmu.privilegegateway.annotation.model.VoObject;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.NewUserVo;
import cn.edu.xmu.privilegegateway.annotation.util.encript.AES;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 新用户Bo
 * @Author LiangJi@3229
 *
 */
@Data
@NoArgsConstructor
public class NewUser {

    private String email;
    private String mobile;
    private String userName;
    private String password;
    private String avatar;
    private String name;
    private Long departId;
    private String openId;
    private LocalDateTime gmtCreated;

}
