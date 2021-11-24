package cn.edu.xmu.privilegegateway.annotation.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;


/**
 * Redis工具类
 * @author Ming Qiu
 **/
@Component
public class RedisUtil {

    @Autowired
    private RedisTemplate<String, Serializable> redisTemplate;

    /**
     * 删除缓存
     *
     * @param key 可以传一个值 或多个
     */

    public void del(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                redisTemplate.delete(key[0]);
            }else{
                redisTemplate.delete((Collection<String>) CollectionUtils.arrayToList(key));
            }
        }
    }

    /**
     * 普通缓存获取
     *
     * @param key 键
     * @return 值
     */
    public Serializable get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 普通缓存放入
     *
     * @param key   键
     * @param value 值
     * @return true成功 false失败
     */
    public  boolean set(String key, Serializable value, long timeout) {
        if (timeout <= 0) {
            timeout = 60;
        }

        long min = 1;
        long max = timeout / 5;
        try {
            //增加随机数，防止雪崩
            timeout += (long) new Random().nextDouble() * (max - min);
            redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 递减
     * @param key   键
     * @param delta 要减少几(小于0)
     * @return
     */
    public long decr(String key, long delta) {
        if (delta > 0) {
            return redisTemplate.opsForValue().increment(key, -delta);
        }else {
            return 0;
        }
    }

    /**
     * 判断redis中是否存在键值
     * @param key   键
     * @return
     */
    public boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 设置key的过期时间
     * @param key
     * @param timeout
     * @param unit
     * @return
     */
    public Boolean expire(String key, long timeout, TimeUnit unit){
        return redisTemplate.expire(key,timeout,unit);
    }

    /**
     * 获得redis中set集合
     * @param key   键
     * @return
     */
    public Set<Serializable > getSet(String key) {
        return  redisTemplate.opsForSet().members(key);
    }

    /**
     * 将Redis中key对应的值和集合otherKeys中的所有键对应的值并在destKey所对应的集合中
     * @param key
     * @param otherKeys
     * @param destKey
     * @return
     */
    public Long unionAndStoreSet(String key, Collection<String> otherKeys, String destKey){
        return redisTemplate.opsForSet().unionAndStore(key, otherKeys, destKey);
    }

    /**
     * 将keys的值并起来存在destKey中
     * @param keys
     * @param destKey
     * @return
     */
    public Long unionAndStoreSet(Collection<String> keys, String destKey){
        return redisTemplate.opsForSet().unionAndStore(keys, destKey);
    }
    /**
     * 将values加入key的集合中
     * @param key
     * @param values
     * @return
     */
    public Long addSet(String key, Serializable... values) {
        return redisTemplate.opsForSet().add(key, values);
    }
}
