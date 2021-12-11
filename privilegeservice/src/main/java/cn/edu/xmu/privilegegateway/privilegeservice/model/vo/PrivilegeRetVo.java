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
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 权限返回VO
 * @author Ming Qiu
 * @date Created in 2020/11/3 23:34
 **/
@Data
@NoArgsConstructor
public class PrivilegeRetVo {

    private Long id;

    private String name;

    private String url;

    private Byte requestType;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "GMT+8")
    private LocalDateTime gmtCreate;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "GMT+8")
    private LocalDateTime gmtModified;

    private UserSimpleRetVo creator;

    private UserSimpleRetVo modifier;

    private Integer sign;

    public PrivilegeRetVo(Privilege obj){
        this.id = obj.getId();
        this.name = obj.getName();
        this.url = obj.getUrl();
        this.requestType = obj.getRequestType().getCode();
        this.gmtCreate = obj.getGmtCreate();
        this.gmtModified = obj.getGmtModified();
    }
}
