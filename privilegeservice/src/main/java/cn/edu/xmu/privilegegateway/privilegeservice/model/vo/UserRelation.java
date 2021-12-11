package cn.edu.xmu.privilegegateway.privilegeservice.model.vo;

import io.swagger.annotations.ApiModel;
import lombok.*;

@Data
@ApiModel
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserRelation {
    private Long id;
    private String name;
    private String gmtCreate;
    private String gmtModified;
    private UserSimpleRetVo creator;
    private UserSimpleRetVo modifier;
    private Byte sign;
}
