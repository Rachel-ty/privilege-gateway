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

package cn.edu.xmu.privilegegateway.privilegeservice.model.bo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 *
 * @author BingShuai Liu
 * @studentId 22920192204245
 * @date 2021/11/28/19:24
 */
@Data
@NoArgsConstructor
public class NewUserBo {
    private Long id;
    private String userName;
    private String mobile;
    private String email;
    private String name;
    private String avatar;
    private String openId;
    private Long departId;
    private String password;
    private Long creatorId;
    private LocalDateTime gmtCreate;
    private Long modifierId;
    private LocalDateTime gmtModified;
    private String idNumber;
    private String passportNumber;
    private String signature;
    private String creatorName;
    private String modifierName;
    private Integer level;
}
