package cn.edu.xmu.privilegegateway.annotation.gateway.microservice;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;


/**
 * @author YuJie 22920192204242
 * @date 2021/11/18
 */
@FeignClient(value = "IGatewayService")
public interface IGatewayService {

    @PostMapping("users/{userId}/token/{jwt}")
    void loadSingleUserPriv(Long userId, String jwt);
}
