package cn.edu.xmu.privilegegateway.privilegeservice.controller;

import cn.edu.xmu.privilegegateway.annotation.util.JacksonUtil;
import cn.edu.xmu.privilegegateway.annotation.util.JwtHelper;
import cn.edu.xmu.privilegegateway.annotation.util.RedisUtil;
import cn.edu.xmu.privilegegateway.privilegeservice.PrivilegeServiceApplication;
import cn.edu.xmu.privilegegateway.privilegeservice.dao.GroupDao;
import cn.edu.xmu.privilegegateway.privilegeservice.dao.PrivilegeDao;
import cn.edu.xmu.privilegegateway.privilegeservice.dao.RoleDao;
import cn.edu.xmu.privilegegateway.privilegeservice.dao.UserDao;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.UserPo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.LoginVo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.ModifyUserVo;
import org.apache.http.entity.ContentType;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author zihan zhou
 * @date 2021/12/6
 */
@AutoConfigureMockMvc
@Transactional
@SpringBootTest(classes = PrivilegeServiceApplication.class)
public class PrivilegeImpactTest {







    private static String token;
    private static String pToken;
    private static String adminToken;
    private static JwtHelper jwtHelper = new JwtHelper();
    public final static String ROLEKEY = "r_%d";
    @MockBean
    private HttpServletRequest request;
    @Autowired
    private RoleDao roleDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    GroupDao groupDao;
    @Autowired
    PrivilegeDao privilegeDao;
    @Autowired
    private MockMvc mvc;
    @MockBean
    private RedisUtil redisUtil;

    public final static String GROUPKEY = "g_%d";

    private final static String USERKEY = "u_%d";

    @BeforeEach
    void init() {
        token = jwtHelper.createToken(46L, "个", 0L, 1, 36000);
        pToken = jwtHelper.createToken(60L, "pikaas", 0L, 1, 36000);
        adminToken = jwtHelper.createToken(1L, "13088admin", 0L, 1, 36000);
    }
    @Test
    public void privilegeImpactTest() throws JSONException {

        String except="[r_2, u_1, g_10, r_3, g_11, r_6, g_12, r_5, g_13, u_51, u_3123, u_4356, u_60, u_2234, u_59, u_49, u_57, r_23]";
        String result=((HashSet)privilegeDao.privilegeImpact(20L)).toString();
        JSONAssert.assertEquals(result, except,false);
    }
    //不存在的privilegeId
    @Test
    public void privilegeImpactTest2() throws JSONException {

        String except="[]";
        String result=((HashSet)privilegeDao.privilegeImpact(31223L)).toString();
        JSONAssert.assertEquals(result, except,false);
    }
}