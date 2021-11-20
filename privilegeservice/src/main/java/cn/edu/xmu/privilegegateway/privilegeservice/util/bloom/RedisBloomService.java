package cn.edu.xmu.privilegegateway.privilegeservice.util.bloom;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xu cang bai
 * @date 2021/11/20
 * @param <T>
 */
public class RedisBloomService<T> {
    private RedisTemplate redisTemplate;

    public RedisBloomService(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * @param bloomFilterName 这里的bloomFilterName 需要为 name + "BloomFilter"
     * @return
     */
    public boolean bloomFilterDelete(String bloomFilterName)
    {
        return redisTemplate.delete(bloomFilterName);
    }

    public Boolean bloomFilterAdd(String bloomFilterName,T value){
        DefaultRedisScript<Boolean> bloomAdd = new DefaultRedisScript<>();
        bloomAdd.setScriptSource(new ResourceScriptSource(new ClassPathResource("bloomFilterAdd.lua")));
        bloomAdd.setResultType(Boolean.class);
        List<Object> keyList= new ArrayList<>();
        keyList.add(bloomFilterName);
        keyList.add(value+"");
        Boolean result = (Boolean) redisTemplate.execute(bloomAdd,keyList);
        return result;
    }

    public Boolean bloomFilterExists(String bloomFilterName,T value) {
        DefaultRedisScript<Boolean> bloomExists = new DefaultRedisScript<>();
        bloomExists.setScriptSource(new ResourceScriptSource(new ClassPathResource("bloomFilterExist.lua")));
        bloomExists.setResultType(Boolean.class);
        List<Object> keyList = new ArrayList<>();
        keyList.add(bloomFilterName);
        keyList.add(value + "");
        Boolean result = (Boolean) redisTemplate.execute(bloomExists, keyList);
        return result;
    }
}
