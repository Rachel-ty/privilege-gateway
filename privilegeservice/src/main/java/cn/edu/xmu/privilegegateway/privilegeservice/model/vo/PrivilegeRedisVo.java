package cn.edu.xmu.privilegegateway.privilegeservice.model.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;
@Data
@ApiModel("权限传值对象")
public class PrivilegeRedisVo {
    @NotNull(message = "url不得为空")
    private String url;
    @NotNull(message = "requestType不得为空")
    @Range(min = 0, max = 3, message = "错误的requestType数值")
    private Byte requestType;
}
