package cn.edu.xmu.privilegegateway.privilegeservice.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created with IntelliJ IDEA.
 *
 * @author BingShuai Liu
 * @studentId 22920192204245
 * @date 2021/12/01/12:48
 * modified by xiuchen lang
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("代理用户信息视图")
public class UserProxySimpleRetVo {
    @ApiModelProperty(name = "用户id", value = "id")
    private Long id;

    @ApiModelProperty(name = "用户名", value = "name")
    private String name;

}
