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

import cn.edu.xmu.privilegegateway.annotation.model.bo.Category;
import cn.edu.xmu.privilegegateway.annotation.model.bo.CategoryRetVo;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CopyAttributeTest {

    @Test
    void test() {
        Category categoryBo=new Category();
        categoryBo.setId(1L);
        categoryBo.setCommissionRatio(1);
        categoryBo.setCreatorId(2L);
        categoryBo.setCreatorName("CreateName");
        categoryBo.setModifierId(3L);
        categoryBo.setModifierName("ModiName");
        LocalDateTime gmtCreate=LocalDateTime.now().minusDays(1);
        LocalDateTime gmtModified=LocalDateTime.now();
        categoryBo.setGmtCreate(gmtCreate);
        categoryBo.setGmtModified(gmtModified);
        categoryBo.setPid(2L);
        categoryBo.setName("name");


        CategoryRetVo categoryRetVo = new CategoryRetVo();
        categoryRetVo.setName("hello");
        categoryRetVo.setCommissionRate(100);
        categoryRetVo.setGmtCreate(gmtCreate);
        Common.copyAttribute(categoryRetVo,categoryBo);

        assertEquals(1L,categoryBo.getId());
        assertEquals("hello",categoryBo.getName());
        assertEquals(1,categoryBo.getCommissionRatio());
        assertEquals(gmtCreate,categoryBo.getGmtCreate());
        assertEquals(2L,categoryBo.getCreatorId());
        assertEquals(3L,categoryBo.getModifierId());
    }
}
