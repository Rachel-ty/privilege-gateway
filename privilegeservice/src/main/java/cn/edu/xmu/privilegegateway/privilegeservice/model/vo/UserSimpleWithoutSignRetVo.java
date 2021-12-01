package cn.edu.xmu.privilegegateway.privilegeservice.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 王文凯
 * @date 2021/12/1
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSimpleWithoutSignRetVo {
    @ApiModelProperty(name = "用户id", value = "id")
    private Long id;

    @ApiModelProperty(name = "用户名", value = "name")
    private String name;
}
