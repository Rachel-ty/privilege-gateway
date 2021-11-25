package cn.edu.xmu.privilegegateway.privilegeservice.controller;

import cn.edu.xmu.privilegegateway.annotation.util.JwtHelper;
import cn.edu.xmu.privilegegateway.privilegeservice.PrivilegeServiceApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import java.lang.*;
/**
 * @author xiuchen lang 22920192204222
 * @date 2021/11/25 14:07
 */
@AutoConfigureMockMvc
@SpringBootTest(classes = PrivilegeServiceApplication.class)   //标识本类是一个SpringBootTest
public class PrivilegeControllerTest {
    private static String adminToken;
    private static JwtHelper jwtHelper = new JwtHelper();

    @Autowired
    private MockMvc mvc;

    @Test
    public void test(){
        adminToken=jwtHelper.createToken(46L,"lxc",0L,1,36000);
        System.out.println(adminToken);
    }
}
