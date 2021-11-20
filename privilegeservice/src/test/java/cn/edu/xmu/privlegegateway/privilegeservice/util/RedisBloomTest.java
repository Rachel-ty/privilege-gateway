package cn.edu.xmu.privlegegateway.privilegeservice.util;

import cn.edu.xmu.privilegegateway.privilegeservice.controller.PrivilegeController;
import cn.edu.xmu.privilegegateway.privilegeservice.util.bloom.RedisBloomFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = PrivilegeController.class)
public class RedisBloomTest {
    @Autowired
    private RedisBloomFilter redisBloomFilter;

    @Test
    void test(){
        redisBloomFilter.bloomFilterDelete("test");

        boolean test0=redisBloomFilter.bloomFilterReserve("test",0.005,1000);
        assertEquals(true,test0);

        boolean test1 = redisBloomFilter.bloomFilterExists("test","test");
        assertEquals(false,test1);

        boolean test2 = redisBloomFilter.bloomFilterAdd("test","test");
        assertEquals(true,test2);

        boolean test3 = redisBloomFilter.bloomFilterExists("test","test");
        assertEquals(true,test3);

        redisBloomFilter.bloomFilterDelete("test");

    }
}
