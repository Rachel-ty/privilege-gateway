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

import cn.edu.xmu.privilegegateway.privilegeservice.model.bo.Role;
import cn.edu.xmu.privilegegateway.privilegeservice.model.bo.User;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理员状态VO
 * @author LiangJi3229
 * @date 2020/11/10 18:41
 */
@Data
@NoArgsConstructor
public class StateVo {
    private Long Code;

    private String name;

    public StateVo(User.State state) {
        Code = Long.valueOf(state.getCode());
        name = state.getDescription();
    }

    public StateVo(Role.State state) {
        Code = Long.valueOf(state.getCode());
        name = state.getDescription();
    }
}
