/**
 * Copyright School of Informatics Xiamen University
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

package cn.edu.xmu.privilegegateway.gateway.localfilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Ming Qiu
 * @date Created in 2020/11/13 22:40
 * modifiedBy Ming Qiu 2021/12/03 12:24
 **/
@Component
public class AuthGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthFilter.Config> {

    private  static  final Logger logger = LoggerFactory.getLogger(AuthGatewayFilterFactory.class);

    @Autowired
    private RedisTemplate<String, Serializable> redisTemplate;

    @Autowired
    private WebClient webClient;

    @Value("${privilegegateway.jwtExpire:3600}")
    private Integer jwtExpireTime = 3600;

    @Value("${privilegegateway.refreshJwtTime:60}")
    private Integer refreshJwtTime = 60;

    @Value("${privilegegateway.retry:3}")
    private Integer maxTime = 3;

    public AuthGatewayFilterFactory() {
        super(AuthFilter.Config.class);
    }

    @Override
    public GatewayFilter apply(AuthFilter.Config config) {
        AuthFilter authFilter = new AuthFilter(config);
        authFilter.setJwtExpireTime(jwtExpireTime);
        authFilter.setRefreshJwtTime(refreshJwtTime);
        authFilter.setRedisTemplate(redisTemplate);
        authFilter.setWebClient(webClient);
        authFilter.setMaxTimes(maxTime);
        return authFilter;
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return new ArrayList<String>(Collections.singleton("tokenName"));
    }

}
