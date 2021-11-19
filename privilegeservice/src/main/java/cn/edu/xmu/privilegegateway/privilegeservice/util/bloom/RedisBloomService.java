package cn.edu.xmu.privilegegateway.privilegeservice.util.bloom;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.ArrayList;
import java.util.List;

public class RedisBloomService<T> {
    private RedisTemplate redisTemplate;

    private String bloomFilterExistLua="local bloomName = KEYS[1]\n" +
            "local value = KEYS[2]\n" +
            "local result_1 = redis.call('BF.EXISTS', bloomName, value)\n" +
            "return result_1";
    private String bloomFilterAddLua="local bloomName = KEYS[1]\n" +
            "local value = KEYS[2]\n" +
            "local result_1 = redis.call('BF.ADD', bloomName, value)\n" +
            "return result_1";
    public RedisBloomService(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Boolean bloomFilterAdd(String bloomFilterName,T value){
        DefaultRedisScript<Boolean> bloomAdd = new DefaultRedisScript<>();
        bloomAdd.setScriptText(bloomFilterAddLua);
        bloomAdd.setResultType(Boolean.class);
        List<Object> keyList= new ArrayList<>();
        keyList.add(bloomFilterName);
        keyList.add(value+"");
        Boolean result = (Boolean) redisTemplate.execute(bloomAdd,keyList);
        return result;
    }



    public Boolean bloomFilterExists(String bloomFilterName,T value) {
        DefaultRedisScript<Boolean> bloomExists = new DefaultRedisScript<>();
        bloomExists.setScriptText(bloomFilterExistLua);
        bloomExists.setResultType(Boolean.class);
        List<Object> keyList = new ArrayList<>();
        keyList.add(bloomFilterName);
        keyList.add(value + "");
        Boolean result = (Boolean) redisTemplate.execute(bloomExists, keyList);
        return result;
    }
}
