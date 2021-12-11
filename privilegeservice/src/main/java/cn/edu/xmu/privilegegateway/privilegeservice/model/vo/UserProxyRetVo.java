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
package cn.edu.xmu.privilegegateway.privilegeservice.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author xiuchen lang 22920192204222
 * @date 2021/11/23 20:19
 */
@Data
@NoArgsConstructor
public class UserProxyRetVo {
    private Long id;
    private UserProxySimpleRetVo user;
    private UserProxySimpleRetVo proxyUser;
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime beginDate;
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime endDate;
    private Byte valid;
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime gmtCreate;
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime gmtModified;
    private UserProxySimpleRetVo creator;
    private UserProxySimpleRetVo modifier;
    private Byte sign;
}
