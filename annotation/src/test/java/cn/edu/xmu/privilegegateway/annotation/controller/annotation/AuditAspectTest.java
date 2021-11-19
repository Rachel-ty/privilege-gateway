package cn.edu.xmu.privilegegateway.annotation.controller.annotation;

import cn.edu.xmu.privilegegateway.annotation.AnnotationTestApplication;
import cn.edu.xmu.privilegegateway.annotation.util.JwtHelper;
import cn.edu.xmu.privilegegateway.annotation.util.JwtHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author zihan zhou
 * @date 2021/11/17
 */

@AutoConfigureMockMvc
@SpringBootTest(classes = AnnotationTestApplication.class)   //标识本类是一个SpringBootTest
public class AuditAspectTest {
    private static String adminToken;
    private static JwtHelper jwtHelper = new JwtHelper();

    @Autowired
    private MockMvc mvc;

    @Test
    public void  auditTest() throws Exception{
        adminToken =jwtHelper.createToken(1L,"admin",1L, 1,3600);
            String responseString = this.mvc.perform(get(
                    "/privilege/shops/1").header("authorization", adminToken).contentType("application/json;charset=UTF-8"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andReturn().getResponse().getContentAsString();
            String expectedResponse="{ \"data\":1 1 admin 1}";
            assert  expectedResponse.equals(responseString);


    }
    @Test
    public void  auditTest2() throws Exception{
        adminToken =jwtHelper.createToken(1L,"admin",0L, 1,3600);
        String responseString = this.mvc.perform(get(
                        "/privilege/shops/0").header("authorization", adminToken).contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectedResponse="{ \"data\":1 0 admin 1}";
        assert  expectedResponse.equals(responseString);


    }
    @Test
    public void  auditTest3() throws Exception{
        adminToken =jwtHelper.createToken(1L,"admin",0L, 1,3600);
        String responseString = this.mvc.perform(get(
                        "/privilege/try/0").header("authorization", adminToken).contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectedResponse="{ \"data\":1 0 admin 1}";
        assert  expectedResponse.equals(responseString);


    }
    @Test
    public void  auditTest4() throws Exception{
        adminToken =jwtHelper.createToken(1L,"admin",1L, 1,3600);
        String responseString = this.mvc.perform(get(
                        "/privilege/try/3").header("authorization", adminToken).contentType("application/json;charset=UTF-8"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectedResponse="{\"errno\":503,\"errmsg\":\"departId不匹配\"}";
        assert  expectedResponse.equals(responseString);


    }
    @Test
    public void  auditTest5() throws Exception{
        adminToken =jwtHelper.createToken(1L,"admin",1L,1, 3600);
        String responseString = this.mvc.perform(get(
                        "/privilege/try1/1").header("authorization", adminToken).contentType("application/json;charset=UTF-8"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectedResponse="{\"errno\":503,\"errmsg\":\"departId不匹配\"}";
        assert  expectedResponse.equals(responseString);


    }
}
