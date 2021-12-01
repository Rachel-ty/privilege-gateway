package cn.edu.xmu.privilegegateway.privilegeservice.model.vo;

import cn.edu.xmu.privilegegateway.privilegeservice.model.bo.Role;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 角色返回视图
 *
 * @author 24320182203281 王纬策
 * createdBy 王纬策 2020/11/04 13:57
 * modifiedBy 王纬策 2020/11/7 19:20
 **/
@Data
@NoArgsConstructor
@ApiModel(description = "角色视图对象")
public class RoleRetVo {
    @ApiModelProperty(value = "角色id")
    private Long id;

    @ApiModelProperty(value = "角色名称")
    private String name;

    @ApiModelProperty(value = "角色描述")
    private String descr;

    @ApiModelProperty(value = "部门id")
    private Long departId;

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

    @ApiModelProperty(value = "角色关系签名标记")
    private Byte sign;

    /**
     * 用Role对象建立Vo对象
     *
     * @author 24320182203281 王纬策
     * @param role role
     * @return RoleRetVo
     * createdBy 王纬策 2020/11/04 13:57
     * modifiedBy 王纬策 2020/11/7 19:20
     */
    public RoleRetVo(Role role) {
        this.id = role.getId();
        this.name = role.getName();
        this.descr = role.getDescr();
        this.departId = role.getDepartId();
        this.gmtCreate = role.getGmtCreate();
        this.gmtModified = role.getGmtModified();
    }
}
