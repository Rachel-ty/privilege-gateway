package cn.edu.xmu.privilegegateway.privilegeservice.microservice;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @title IGatewayServiceImpl.java
 * @description 网关内部调用实现
 * @author wwc
 * @date 2020/12/01 23:17
 * @version 1.0
 */

@FeignClient(value = "IGatewayService")
public interface IGatewayService {

    @PostMapping("users/{userId}/privilege/{jwt}")
    void loadSingleUserPriv(Long userId, String jwt);
}
