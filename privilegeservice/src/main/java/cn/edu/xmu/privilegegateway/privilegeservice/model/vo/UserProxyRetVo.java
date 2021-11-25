package cn.edu.xmu.privilegegateway.privilegeservice.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * @author xiuchen lang 22920192204222
 * @date 2021/11/23 20:19
 */
@Data
@AllArgsConstructor
public class UserProxyRetVo {
    private Long id;
    private UserSimpleRetVo user;
    private UserSimpleRetVo proxyUser;
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime beginDate;
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endDate;
    private Byte valid;
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime gmtCreate;
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime gmtModified;
    private UserSimpleRetVo creator;
    private UserSimpleRetVo modifier;
    public UserProxyRetVo(){
        user= new UserSimpleRetVo();
        proxyUser= new UserSimpleRetVo();
        creator= new UserSimpleRetVo();
        modifier= new UserSimpleRetVo();
    }
}
