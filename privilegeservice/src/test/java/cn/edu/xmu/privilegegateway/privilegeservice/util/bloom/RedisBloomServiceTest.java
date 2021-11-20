package cn.edu.xmu.privilegegateway.privilegeservice.util.bloom;

import cn.edu.xmu.privilegegateway.privilegeservice.PrivilegeServiceApplication;
import cn.edu.xmu.privilegegateway.privilegeservice.dao.NewUserDao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = PrivilegeServiceApplication.class)
@AutoConfigureMockMvc
@Transactional
class RedisBloomServiceTest {
    @Autowired
    RedisTemplate redisTemplate;

    RedisBloomService bloomFilter;
    String suffix ="BloomFilter";
    @Test
    void Test()
    {
        bloomFilter=new RedisBloomService(redisTemplate);
        bloomFilter.bloomFilterAdd("test"+suffix,"testValue");
        boolean result1=bloomFilter.bloomFilterExists("test"+suffix,"testValue");
        boolean result2=bloomFilter.bloomFilterExists("test"+suffix,"Value");
        assertEquals(true,result1);
        assertEquals(false,result2);
    }

}