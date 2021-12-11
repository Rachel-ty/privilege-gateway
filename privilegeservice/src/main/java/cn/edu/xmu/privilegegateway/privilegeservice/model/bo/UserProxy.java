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
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户代理Bo类
 *
 * @author 24320182203221 李狄翰
 * createdBy 李狄翰2020/11/09 12:00
 * modifiedBy 郎秀晨 22920192204222 2021/11/23
 **/
@Data
@NoArgsConstructor
public class UserProxy{
    private Long id;
    private Long userId;
    private Long proxyUserId;
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime beginDate;
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime endDate;
    private String signature;
    private Byte valid;
    private Long departId;
    private Long creatorId;
    private String creatorName;
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime gmtCreate;
    private Long modifierId;
    private String modifierName;
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime gmtModified;
    private String userName;
    private String proxyUserName;
    private Byte sign;

}
