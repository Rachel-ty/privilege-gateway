package cn.edu.xmu.privilegegateway.annotation.config;

import cn.edu.xmu.privilegegateway.annotation.util.bloom.BloomFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BloomConfig {

    @Bean
    public BloomFilter<String> stringBloomFilter() {
        return new BloomFilter<>();
    }

    @Bean
    public BloomFilter<Long> longBloomFilter() {
        return new BloomFilter<>();
    }

}
