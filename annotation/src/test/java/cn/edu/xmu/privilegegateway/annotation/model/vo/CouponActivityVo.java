package cn.edu.xmu.privilegegateway.annotation.model.vo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.ZonedDateTime;

/**
 * @author RenJieZheng 22920192204334
 */
@Data
@AllArgsConstructor
@NoArgsConstructor

public class CouponActivityVo {
    private String name;
    private Integer quantity;
    private Byte quantityType;
    private Byte validTerm;
    private ZonedDateTime couponTime;
    private ZonedDateTime beginTime;
    private ZonedDateTime endTime;
    private String strategy;
}
