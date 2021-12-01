package cn.edu.xmu.privilegegateway.privilegeservice.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author xiuchen lang 22920192204222
 * @date 2021/11/23 20:19
 */
@Data
@AllArgsConstructor
public class UserProxyRetVo {
    private Long id;
    private UserProxySimpleRetVo user;
    private UserProxySimpleRetVo proxyUser;
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime beginDate;
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime endDate;
    private Byte valid;
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime gmtCreate;
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime gmtModified;
    private UserProxySimpleRetVo creator;
    private UserProxySimpleRetVo modifier;
    private Byte sign;
    public UserProxyRetVo(){
        user= new UserProxySimpleRetVo();
        proxyUser= new UserProxySimpleRetVo();
        creator= new UserProxySimpleRetVo();
        modifier= new UserProxySimpleRetVo();
    }
}
