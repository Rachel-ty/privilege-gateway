package cn.edu.xmu.privilegegateway.privilegeservice.model.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Created with IntelliJ IDEA.
 *
 * @author BingShuai Liu
 * @studentId 22920192204245
 * @date 2021/11/28/21:04
 */
@Data
public class ConclusionVo {
    @NotNull(message = "审核结果不能为空")
    private Boolean conclusion;
    @NotNull(message = "用户等级不能为空")
    private Integer level;
}
