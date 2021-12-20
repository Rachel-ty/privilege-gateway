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

package cn.edu.xmu.privilegegateway.annotation.util;

import cn.edu.xmu.privilegegateway.annotation.model.vo.UserRetVo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JacksonUtilTest {
    @Test
    public void parseSubnodeToObjectTest(){
        String ret = "{\"errno\":0,\"data\":{\"total\":1,\"pages\":1,\"pageSize\":1,\"page\":1,\"list\":[{\"id\":1,\"name\":\"平台超级管理员\",\"descr\":null,\"departId\":0,\"creator\":{\"id\":1,\"name\":\"admin\"},\"modifier\":{\"id\":null,\"name\":null},\"sign\":0}]},\"errmsg\":\"成功\"}";
        Long id = JacksonUtil.parseSubnodeToObject(ret, "/data/list/0/id", Long.class);
        assertEquals(1L, id);
    }

    @Test
    public void parseSubnodeToObjectListTest(){
        String ret = "{\"errno\":0,\"data\":{\"total\":1,\"pages\":1,\"pageSize\":1,\"page\":1,\"list\":[{\"id\":1,\"name\":\"平台超级管理员\",\"descr\":null,\"departId\":0,\"creator\":{\"id\":1,\"name\":\"admin\"},\"modifier\":{\"id\":null,\"name\":null},\"sign\":0}]},\"errmsg\":\"成功\"}";
        List<UserRetVo> vos = JacksonUtil.parseSubnodeToObjectList(ret, "/data/list", UserRetVo.class);
        assertEquals(1L, vos.get(0).getId());
    }
}
