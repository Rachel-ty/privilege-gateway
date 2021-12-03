package cn.edu.xmu.privilegegateway.privilegeservice.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel(description = "角色继承视图对象")
public class RoleInheritedRetVo {
    @ApiModelProperty(value = "子角色id")
    private Long id;

    @ApiModelProperty(value = "子角色名称")
    private String name;

    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS")
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime gmtCreate;

    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS")
    @ApiModelProperty(value = "修改时间")
    private LocalDateTime gmtModified;

    @ApiModelProperty(value = "创建人")
    private UserSimpleWithoutSignRetVo creator;

    @ApiModelProperty(value = "修改人")
    private UserSimpleWithoutSignRetVo modifier;

    @ApiModelProperty(value = "角色继承签名标记")
    private Byte sign;
}
