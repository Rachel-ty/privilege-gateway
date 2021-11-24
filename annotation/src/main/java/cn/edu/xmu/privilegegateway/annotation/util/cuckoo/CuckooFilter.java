package cn.edu.xmu.privilegegateway.annotation.util.cuckoo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Zhiliang Li
 * @date 2021/11/22
 */
@Component
public class CuckooFilter<T> {
    private static final String ADD_VALUE_PATH = "cuckoo/addValueToFilter.lua";
    private static final String CHECK_VALUE_PATH = "cuckoo/checkValueInFilter.lua";
    private static final String DELETE_FILTER_PATH = "cuckoo/deleteFilter.lua";
    private static final String DELETE_VALUE_PATH = "cuckoo/deleteValueFromFilter.lua";
    private static final String CHECK_FILTER_PATH = "cuckoo/checkFilter.lua";
    private static final String NEW_FILTER_PATH = "cuckoo/newFilter.lua";

    private static final String DEFAULT_CAPACITY = "100";


    @Autowired
    private RedisTemplate<String, Serializable> redis;

    public Boolean addValue(String filterFor, T value) {
        DefaultRedisScript<Boolean> script = new DefaultRedisScript<>();

        script.setScriptSource(new ResourceScriptSource(new ClassPathResource(ADD_VALUE_PATH)));
        script.setResultType(Boolean.class);

        List<String> argList = Stream.of(filterFor, value.toString()).collect(Collectors.toList());

        return redis.execute(script, argList);
    }

    public Boolean checkValue(String filterFor, T value) {
        DefaultRedisScript<Boolean> script = new DefaultRedisScript<>();

        script.setScriptSource(new ResourceScriptSource(new ClassPathResource(CHECK_VALUE_PATH)));
        script.setResultType(Boolean.class);

        List<String> argList = Stream.of(filterFor, value.toString()).collect(Collectors.toList());

        return redis.execute(script, argList);
    }

    public Boolean deleteFilter(String filterFor) {
        DefaultRedisScript<Boolean> script = new DefaultRedisScript<>();

        script.setScriptSource(new ResourceScriptSource(new ClassPathResource(DELETE_FILTER_PATH)));
        script.setResultType(Boolean.class);

        List<String> argList = Stream.of(filterFor).collect(Collectors.toList());

        return redis.execute(script, argList);
    }

    public Boolean deleteValue(String filterFor, T value) {
        DefaultRedisScript<Boolean> script = new DefaultRedisScript<>();

        script.setScriptSource(new ResourceScriptSource(new ClassPathResource(DELETE_VALUE_PATH)));
        script.setResultType(Boolean.class);

        List<String> argList = Stream.of(filterFor, value.toString()).collect(Collectors.toList());

        return redis.execute(script, argList);
    }

    public Boolean checkFilter(String filterFor) {
        DefaultRedisScript<Boolean> script = new DefaultRedisScript<>();

        script.setScriptSource(new ResourceScriptSource(new ClassPathResource(CHECK_FILTER_PATH)));
        script.setResultType(Boolean.class);

        List<String> argList = Stream.of(filterFor).collect(Collectors.toList());

        return redis.execute(script, argList);
    }

    public Boolean newFilter(String filterFor, Integer capacity, Integer bucketSize, Integer maxIterations, Integer expansion) {
        List<String> argList = new ArrayList<>();
        argList.add(filterFor);

        String capacityStr = DEFAULT_CAPACITY;

        DefaultRedisScript<Boolean> script = new DefaultRedisScript<>();

        script.setScriptSource(new ResourceScriptSource(new ClassPathResource(NEW_FILTER_PATH)));
        script.setResultType(Boolean.class);

        if (capacity != null) {
            capacityStr = capacity.toString();
        }
        argList.add(capacityStr);

        /**
         * 添加可选参数bucketsize、maxiterations、expansion
         */
        String[] argsName = {"bucketsize", "maxiterations", "expansion"};
        Integer[] args = {bucketSize, maxIterations, expansion};

        for (int i = 0; i < 3; i++) {
            if (args[i] != null) {
                argList.add(argsName[i]);
                argList.add(args[i].toString());
            }
        }

        return redis.execute(script, argList);
    }
}
