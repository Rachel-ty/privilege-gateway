package cn.edu.xmu.privilegegateway.annotation.aop;

import cn.edu.xmu.privilegegateway.annotation.AnnotationTestApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(classes = AnnotationTestApplication.class)
public class PageAOPTest {
    @Autowired
    private MockMvc mvc;

    @Test
    public void  pageTest() throws Exception{
        String responseString = this.mvc.perform(get(
                        "/pages/1").contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectedResponse="{\"data\":1 10}";
        assert  expectedResponse.equals(responseString);
    }
    @Test
    public void  pageTest2() throws Exception{
        String responseString = this.mvc.perform(get(
                        "/pages/1?page=2&pageSize=5").contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectedResponse="{\"data\":2 5}";
        assert  expectedResponse.equals(responseString);
    }
}
