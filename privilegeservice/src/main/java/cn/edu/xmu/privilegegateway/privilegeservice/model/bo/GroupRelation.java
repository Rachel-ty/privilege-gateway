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

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class GroupRelation {

    private Long id;
    private Long groupPId;
    private Long groupSId;
    private String signature;
    private Long creatorId;
    private LocalDateTime gmtCreate;
    private Long modifierId;
    private LocalDateTime gmtModified;
    private Byte sign;
}
