package cn.edu.xmu.privilegegateway.privilegeservice.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Pattern;

/**
 * 用户信息 Vo
 * @author 19720182203919 李涵
 * Created at 2020/11/4 20:30
 * Modified by 19720182203919 李涵 at 2020/11/5 10:42
 **/
@Data
@ApiModel(description = "管理员用户信息视图对象")
public class UserVo {

    @ApiModelProperty(value = "用户姓名")
    private String name;

    @ApiModelProperty(value = "用户头像 URL")
    private String avatar;

    @ApiModelProperty(value = "用户身份证号")
    private String idNumber;
    @ApiModelProperty(value = "护照号")
    private String passportNumber;
    @ApiModelProperty(value = "用户等级")
    private Integer level;

}
