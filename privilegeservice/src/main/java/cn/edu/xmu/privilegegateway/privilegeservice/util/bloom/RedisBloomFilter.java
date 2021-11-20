package cn.edu.xmu.privilegegateway.privilegeservice.util.bloom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * @author Zhiliang Li
 * @date 2020-11-20
 */

@Component
public class RedisBloomFilter {
	@Autowired
	private RedisTemplate redisTemplate;

	private static double size = Math.pow(2, 32);

	/**
	 * 有序集合获取排名
	 *
	 * @param key
	 */
	public Set<ZSetOperations.TypedTuple<Object>> reverseZRankWithRank(String key, long start, long end) {
		ZSetOperations<String, Object> zset = redisTemplate.opsForZSet();
		Set<ZSetOperations.TypedTuple<Object>> ret = zset.reverseRangeWithScores(key, start, end);
		return ret;
	}

	public Boolean bloomFilterAdd(String bloomFilterName, String value) {
		DefaultRedisScript<Boolean> bloomAdd = new DefaultRedisScript<>();
		bloomAdd.setScriptSource(new ResourceScriptSource(new ClassPathResource("bloomFilterAdd.lua")));
		bloomAdd.setResultType(Boolean.class);
		List<Object> keyList = new ArrayList<>();
		keyList.add("bloom" + bloomFilterName);
		keyList.add(value);
		Boolean result = (Boolean) redisTemplate.execute(bloomAdd, keyList);
		return result;
	}

	public Boolean bloomFilterExists(String bloomFilterName, String value) {
		DefaultRedisScript<Boolean> bloomExists = new DefaultRedisScript<>();
		bloomExists.setScriptSource(new ResourceScriptSource(new ClassPathResource("bloomFilterExist.lua")));
		bloomExists.setResultType(Boolean.class);
		List<Object> keyList = new ArrayList<>();
		keyList.add("bloom" + bloomFilterName);
		keyList.add(value);
		Boolean result = (Boolean) redisTemplate.execute(bloomExists, keyList);
		return result;
	}

	public Boolean bloomFilterDelete(String key, Double errorRate,Integer capacity) {
		redisTemplate.delete("bloom" + key);

		DefaultRedisScript<Boolean> bloomExists = new DefaultRedisScript<>();
		bloomExists.setScriptSource(new ResourceScriptSource(new ClassPathResource("bloomFilterReserve.lua")));
		bloomExists.setResultType(Boolean.class);
		List<Object> keyList = new ArrayList<>();
		keyList.add("bloom" + key);
		keyList.add(errorRate+"");
		keyList.add(capacity+"");
		Boolean result = (Boolean) redisTemplate.execute(bloomExists, keyList);

		return result;
	}
}
