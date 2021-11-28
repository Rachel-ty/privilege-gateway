package cn.edu.xmu.privilegegateway.privilegeservice.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 权限传值对象
 *
 * @author Di Han Li
 * @date Created in 2020/11/4 9:08
 * Modified by 24320182203221 李狄翰 at 2020/11/8 8:00
 * Modified by 22920192204222 郎秀晨 at 2021/11/25
 **/
@Data
@ApiModel("用户代理传值对象")
public class UserProxyVo {

    @ApiModelProperty(name = "代理开始时间", value = "beginDate", required = true)
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime beginDate;

    @ApiModelProperty(name = "代理过期时间", value = "beginDate", required = true)
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime endDate;


}
