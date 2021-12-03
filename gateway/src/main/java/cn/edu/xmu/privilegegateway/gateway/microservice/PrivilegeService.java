/**
 * Copyright School of Informatics Xiamen University
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

package cn.edu.xmu.privilegegateway.gateway.microservice;

import cn.edu.xmu.privilegegateway.annotation.util.InternalReturnObject;
import cn.edu.xmu.privilegegateway.gateway.microservice.vo.RequestVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


/**
 * @author YuJie 22920192204242
 * @date 2021/11/18
 * modifiedBy Ming Qiu 2021/12/3 17:14
 */
@FeignClient(value = "privilege-service")
public interface PrivilegeService {

    @PutMapping("/internal/users/{userId}")
    InternalReturnObject loadUserPriv(@PathVariable Long userId);

    @PutMapping("/internal/privileges/load")
    InternalReturnObject loadPrivilege(@RequestBody RequestVo requestVo);
}
