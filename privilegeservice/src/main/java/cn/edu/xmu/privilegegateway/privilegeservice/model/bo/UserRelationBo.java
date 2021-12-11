package cn.edu.xmu.privilegegateway.privilegeservice.model.bo;

import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.UserSimpleRetVo;
import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel
public class UserRelationBo {
    private Long id;
    private String name;
    private String gmtCreate;
    private String gmtModified;
    private UserSimpleRetVo creator;
    private UserSimpleRetVo modifier;
    private Byte sign;
    private String signature;
}
