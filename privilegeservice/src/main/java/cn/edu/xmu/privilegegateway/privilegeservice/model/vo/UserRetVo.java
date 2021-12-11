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

import io.swagger.annotations.ApiModel;
import lombok.*;

import java.time.LocalDateTime;

/**
 * @author Bingshuai Liu
 * @studentId 22920192204245
 */
@Data
@NoArgsConstructor
@ApiModel("用户信息视图")
public class UserRetVo {
    private Long id;
    private String userName;
    private String mobile;
    private String email;
    private String avatar;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
    private Byte state;
    private Long departId;
    private String idNumber;
    private String passportNumber;
    private UserSimpleRetVo creator;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;
    private UserSimpleRetVo modifier;
    private Byte sign;
}
