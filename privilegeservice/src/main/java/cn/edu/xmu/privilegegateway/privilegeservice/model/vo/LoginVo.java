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
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;

/**
 * @auther mingqiu
 * @date 2020/6/27 下午7:54
 */
@ApiModel
@Data
@NoArgsConstructor
public class LoginVo {
    @NotBlank(message = "必须输入用户名")
    @ApiModelProperty(name = "用户名", value = "testuser")
    private String userName;

    @NotBlank(message = "必须输入密码")
    @ApiModelProperty(name = "密码", value = "123456r")
    private String password;
}
