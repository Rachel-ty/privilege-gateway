package cn.edu.xmu.privilegegateway.annotation.util;


import cn.edu.xmu.privilegegateway.annotation.model.bo.Category;
import cn.edu.xmu.privilegegateway.annotation.model.bo.CategoryRetVo;
import cn.edu.xmu.privilegegateway.annotation.model.bo.Shop;
import cn.edu.xmu.privilegegateway.annotation.model.vo.ShopRetVo;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CloneVoTest {

    /**
     * @author xucangbai
     * @date 2021/11/13
     */
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


        CategoryRetVo categoryRetVo = (CategoryRetVo) Common.cloneVo(categoryBo, CategoryRetVo.class);
        assertEquals(categoryRetVo.getId(),1L);
        assertEquals(categoryRetVo.getName(),"name");

        assertEquals(categoryRetVo.getCreator().getId(),2L);
        assertEquals(categoryRetVo.getCreator().getName(),"CreateName");
        assertEquals(categoryRetVo.getModifier().getId(),3L);
        assertEquals(categoryRetVo.getModifier().getName(),"ModiName");
        assertEquals(categoryRetVo.getGmtCreate(),gmtCreate);
        assertEquals(categoryRetVo.getGmtModified(),gmtModified);
    }

    @Test
    void test2() {
        Shop shop=new Shop();
        shop.setState(Shop.State.ONLINE);

        ShopRetVo shopRetVoTest = (ShopRetVo) Common.cloneVo(shop, ShopRetVo.class);

        //枚举转Byte
        assertEquals(Byte.valueOf("2"),shopRetVoTest.getState());

        //Byte转枚举
        shopRetVoTest.setState(Byte.valueOf("2"));
        Shop shop1= (Shop) Common.cloneVo(shopRetVoTest,Shop.class);
        assertEquals(Shop.State.ONLINE,shop1.getState());
    }
}