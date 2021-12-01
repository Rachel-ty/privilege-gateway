package cn.edu.xmu.privilegegateway.annotation.util;


import cn.edu.xmu.privilegegateway.annotation.model.UserPo;
import cn.edu.xmu.privilegegateway.annotation.util.coder.BaseCoder;
import cn.edu.xmu.privilegegateway.annotation.util.coder.imp.AESCoder;
import cn.edu.xmu.privilegegateway.annotation.util.coder.imp.SHA256Sign;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 * @author RenJieZheng 22920192204334
 * @date 2021/11/30
 */
public class ListDecode {
    private BaseCoder coder = new AESCoder(new SHA256Sign(), "OOAD2020-11-01");

    @Test
    public void testListDecode() throws Exception{
        UserPo userPo = new UserPo();
        userPo.setId(1L);
        userPo.setName("6E1A67D0B62D40F1145CA7FCA54AD036");
        userPo.setUserName("zhangsan");
        userPo.setMobile("19E59DE959DE472ABECEC38A0219689A");
        userPo.setEmail("167A3FDD80B03DD24B9FBA3B08574263");
        userPo.setPassword("BCB71451C344BFB09FC0403699098E9E");
        userPo.setOpenId("12345");
        userPo.setState((byte) 1);
        userPo.setDepartId(123L);
        userPo.setCreatorId(1L);
        userPo.setSignature("85039d0bd948c82d81322ee21b0865ec2f78f1a7656189db39663d0e72cbb07d");

        Collection<String>  codeFields = new ArrayList<>(Arrays.asList("password", "name", "email", "mobile"));
        List<String> signFields = new ArrayList<>(Arrays.asList("password", "name", "email", "mobile","state","departId","level"));
        List<UserPo>list1 = Arrays.asList(userPo);
        List<UserPo>list2 = (List<UserPo>) Common.listDecode(list1,UserPo.class,coder,codeFields,signFields,"signature");
        assertNotNull(list2);
        assertEquals(1L,list2.get(0).getId());
    }
}
