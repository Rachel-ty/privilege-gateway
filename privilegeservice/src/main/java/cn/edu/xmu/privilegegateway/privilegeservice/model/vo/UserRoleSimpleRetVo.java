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
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
用户角色简单返回对象
@author 张晖婧
 */

@Data
@NoArgsConstructor
@ApiModel
public class UserRoleSimpleRetVo {
    @ApiModelProperty(name = "角色id", value = "id")
    private Long id;

    @ApiModelProperty(name = "角色名", value = "name")
    private String name;

    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS")
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime gmtCreate;

    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS")
    @ApiModelProperty(value = "修改时间")
    private LocalDateTime gmtModified;

    @ApiModelProperty(value = "创建人")
    private UserSimpleRetVo creator;

    @ApiModelProperty(value = "修改人")
    private UserSimpleRetVo modifier;

    @ApiModelProperty(value = "用户角色签名标记")
    private Byte sign;
}
