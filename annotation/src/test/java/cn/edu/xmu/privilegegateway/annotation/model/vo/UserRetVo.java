package cn.edu.xmu.privilegegateway.annotation.model.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SimpleUserRetVo
 *
 * @author Zhiliang Li 22920192204235
 * @date 2021/11/12
 */
@Data
@NoArgsConstructor
public class UserRetVo {
    private Long id;
    private String name;
    private String descr;
    private Long departId;
    private SimpleUserRetVo creator;
    private SimpleUserRetVo modifier;
    private Integer sign;
}
