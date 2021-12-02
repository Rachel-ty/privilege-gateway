package cn.edu.xmu.privilegegateway.privilegeservice.model.bo;
import cn.edu.xmu.privilegegateway.annotation.util.Common;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 用户代理Bo类
 *
 * @author 24320182203221 李狄翰
 * createdBy 李狄翰2020/11/09 12:00
 * modifiedBy 郎秀晨 22920192204222 2021/11/23
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProxy{
    private Long id;
    private Long userId;
    private Long proxyUserId;
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime beginDate;
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime endDate;
    private String signature;
    private Byte valid;
    private Long departId;
    private Long creatorId;
    private String creatorName;
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime gmtCreate;
    private Long modifierId;
    private String modifierName;
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime gmtModified;
    private String userName;
    private String proxyUserName;
    private Byte sign;

}
