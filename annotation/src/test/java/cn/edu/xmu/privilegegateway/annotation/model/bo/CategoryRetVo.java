package cn.edu.xmu.privilegegateway.annotation.model.bo;

import cn.edu.xmu.privilegegateway.annotation.model.vo.SimpleUserRetVo;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 商品分类RetVo
 *
 * @author Zhiliang Li 22920192204235
 * @date 2021/11/18
 */
@Data
@NoArgsConstructor
public class CategoryRetVo {
    private Long id;
    private Integer commissionRate;
    private String name;
    private SimpleUserRetVo creator;
    private SimpleUserRetVo modifier;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;
}
