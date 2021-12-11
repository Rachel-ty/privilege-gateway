package cn.edu.xmu.privilegegateway.annotation.model.bo;

import cn.edu.xmu.privilegegateway.annotation.model.VoObject;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商品分类Bo
 * pid为0表示一级类，大于0表示二级类，为-1表示单独类
 * @author Zhiliang Li 22920192204235
 * @date 2021/11/18
 */
@Data
@NoArgsConstructor
public class Category implements Serializable {
    private Long id;
    private String name;
    private Integer commissionRatio;
    private Long pid;
    private Long creatorId;
    private String creatorName;
    private LocalDateTime gmtCreate;
    private Long modifierId;
    private LocalDateTime gmtModified;
    private String modifierName;


}

