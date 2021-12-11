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

import cn.edu.xmu.privilegegateway.privilegeservice.model.bo.Privilege;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 权限返回简单VO
 * @author Ming Qiu
 * @date Created in 2020/11/7 16:34
 **/
@Data
@NoArgsConstructor
public class PrivilegeSimpleRetVo {

    private Long id;

    private String name;

    public PrivilegeSimpleRetVo(Privilege obj){
        this.id = obj.getId();
        this.name = obj.getName();
    }
}
