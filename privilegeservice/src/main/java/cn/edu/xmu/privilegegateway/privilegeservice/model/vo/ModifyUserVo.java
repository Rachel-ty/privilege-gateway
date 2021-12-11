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
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Pattern;

/**
 * 用户信息 Vo
 * @author 蒋欣雨
 * Created at 2021/12/1
 **/
@Data
@NoArgsConstructor
@ApiModel(description = "用户修改信息视图对象")
public class ModifyUserVo {

    @ApiModelProperty(value = "用户姓名")
    private String name;

    @ApiModelProperty(value = "用户身份证号")
    private String idNumber;
    @ApiModelProperty(value = "护照号")
    private String passportNumber;
    @ApiModelProperty(value = "用户等级")
    private Integer level;
}
