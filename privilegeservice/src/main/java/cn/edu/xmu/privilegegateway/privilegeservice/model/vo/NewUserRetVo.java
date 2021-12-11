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

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 *
 * @author BingShuai Liu
 * @studentId 22920192204245
 * @date 2021/12/10/21:21
 */
@Data
@NoArgsConstructor
public class NewUserRetVo {
    private Long id;
    private String userName;
    private String mobile;
    private String email;
    private String name;
    private String avatar;
    private String openId;
    private Long departId;
    private String password;
    private String idNumber;
    private String passportNumber;
    private UserSimpleRetVo creator;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;
    private UserSimpleRetVo modifier;
    private Integer level;
    private Byte sign;
}
