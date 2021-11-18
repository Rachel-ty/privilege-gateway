package cn.edu.xmu.privilegegateway.privilegeservice.microservice;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;

/**
 * @Author: Yifei Wang 24320182203286
 * @Date: 2020/12/10 9:43
 */

@FeignClient(value = "IGatewayService")
public interface IUserService {

    @PutMapping("users/{userId}/depart/{jwt}")
    boolean changeUserDepart(Long userId, Long departId);

    @GetMapping("users/{userId}/name")
    String getUserName(Long userId);
}
