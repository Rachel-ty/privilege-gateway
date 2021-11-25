package cn.edu.xmu.privilegegateway.annotation.util.bloom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * @author Jianjian Chan
 * @date 2021-11-20
 * @sn 22920192204170
 */
@Component
public class BloomFilter<T> {
    private static final String ADD_VALUE_PATH = "bloom/addValueToFilter.lua";
    private static final String CHECK_VALUE_PATH = "bloom/checkValueInFilter.lua";
    private static final String DELETE_FILTER_PATH = "bloom/deleteFilter.lua";
    private static final String CHECK_FILTER_PATH = "bloom/checkFilter.lua";
    private static final String NEW_FILTER_PATH = "bloom/newFilter.lua";

    private static final String DEFAULT_ERROR_RATE = "0.001";
    private static final String DEFAULT_CAPACITY = "100";

    private static final String SUFFIX = "Filter";

    @Autowired
    private RedisTemplate<String, Serializable> redis;

    public Boolean addValue(String filterFor, T value) {
        DefaultRedisScript<Boolean> script = new DefaultRedisScript<>();

        script.setScriptSource(new ResourceScriptSource(new ClassPathResource(ADD_VALUE_PATH)));
        script.setResultType(Boolean.class);

        List<String> argList = Stream.of(filterFor + SUFFIX, value.toString()).collect(Collectors.toList());

        return redis.execute(script, argList);
    }

    public Boolean checkValue(String filterFor, T value) {
        DefaultRedisScript<Boolean> script = new DefaultRedisScript<>();

        script.setScriptSource(new ResourceScriptSource(new ClassPathResource(CHECK_VALUE_PATH)));
        script.setResultType(Boolean.class);

        List<String> argList = Stream.of(filterFor + SUFFIX, value.toString()).collect(Collectors.toList());

        return redis.execute(script, argList);
    }

    public Boolean deleteFilter(String filterFor) {
        DefaultRedisScript<Boolean> script = new DefaultRedisScript<>();

        script.setScriptSource(new ResourceScriptSource(new ClassPathResource(DELETE_FILTER_PATH)));
        script.setResultType(Boolean.class);

        List<String> argList = Stream.of(filterFor + SUFFIX).collect(Collectors.toList());

        return redis.execute(script, argList);
    }

    public Boolean checkFilter(String filterFor) {
        DefaultRedisScript<Boolean> script = new DefaultRedisScript<>();

        script.setScriptSource(new ResourceScriptSource(new ClassPathResource(CHECK_FILTER_PATH)));
        script.setResultType(Boolean.class);

        List<String> argList = Stream.of(filterFor + SUFFIX).collect(Collectors.toList());

        return redis.execute(script, argList);
    }

    public Boolean newFilter(String filterFor, Double errorRate, Integer capacity) {
        String errorRateStr = DEFAULT_ERROR_RATE;
        String capacityStr = DEFAULT_CAPACITY;

        if(errorRate != null) {
            if(errorRate <= 0 || errorRate >= 1) {
                return false;
            }

            errorRateStr = errorRate.toString();
        }

        if(capacity != null) {
            if(capacity <= 0) {
                return false;
            }

            capacityStr = capacity.toString();
        }

        DefaultRedisScript<Boolean> script = new DefaultRedisScript<>();

        script.setScriptSource(new ResourceScriptSource(new ClassPathResource(NEW_FILTER_PATH)));
        script.setResultType(Boolean.class);

        List<String> argList = Stream.of(filterFor + SUFFIX, errorRateStr, capacityStr).collect(Collectors.toList());

        return redis.execute(script, argList);
    }
}
