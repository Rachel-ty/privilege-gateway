package cn.edu.xmu.privilegegateway.config;

import cn.edu.xmu.privilegegateway.util.bloom.BloomFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BloomConfig {

    @Bean
    public BloomFilter<String> stringBloomFilter() {
        return new BloomFilter<String>();
    }

    @Bean
    public BloomFilter<Long> longBloomFilter() {
        return new BloomFilter<Long>();
    }

}
