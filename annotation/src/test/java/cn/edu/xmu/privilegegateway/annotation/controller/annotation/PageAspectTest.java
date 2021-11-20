package cn.edu.xmu.privilegegateway.annotation.controller.annotation;

import cn.edu.xmu.privilegegateway.annotation.AnnotationTestApplication;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.nio.charset.Charset;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(classes = AnnotationTestApplication.class)   //标识本类是一个SpringBootTest
public class PageAspectTest {
    @Autowired
    private MockMvc mvc;
    final Charset charset=Charset.forName("UTF-8");

    @Test
    public void onlineAdvancesale1() throws Exception {
        String responseString = this.mvc.perform(MockMvcRequestBuilders.get("/privilege/shops").
                        param("page","").
                        param("pageSize","")
                        .contentType("application/json;charset=UTF-8")).
                andExpect(MockMvcResultMatchers.status().isOk()).andDo(print()).andReturn().getResponse().getContentAsString(charset);
        System.out.println(responseString);
        String expectedResponse="{ \"data\":110}";
        JSONAssert.assertEquals(expectedResponse,responseString,true);
    }
    @Test
    public void onlineAdvancesale2() throws Exception {
        String responseString = this.mvc.perform(MockMvcRequestBuilders.get("/privilege/shops")
                        .contentType("application/json;charset=UTF-8")).
                andExpect(MockMvcResultMatchers.status().isOk()).andDo(print()).andReturn().getResponse().getContentAsString(charset);
        System.out.println(responseString);
        String expectedResponse="{ \"data\":110}";
        JSONAssert.assertEquals(expectedResponse,responseString,true);
    }
}
