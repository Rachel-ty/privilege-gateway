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

import cn.edu.xmu.privilegegateway.annotation.util.RedisUtil;
import cn.edu.xmu.privilegegateway.gateway.microservice.PrivilegeService;
import cn.edu.xmu.privilegegateway.gateway.microservice.vo.RequestVo;
import cn.edu.xmu.privilegegateway.annotation.util.JwtHelper;
import cn.edu.xmu.privilegegateway.annotation.util.ReturnNo;
import com.alibaba.fastjson.JSONObject;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Ming Qiu
 * @date Created in 2020/11/13 22:31
 * modifiedBy Ming Qiu 2021/12/3 12:20
 **/
public class AuthFilter implements GatewayFilter, Ordered {
    private static final Logger logger = LoggerFactory.getLogger(AuthFilter.class);

    private static final String USERKEY = "up_%d";
    private static final String PRIVKEY = "%s-%d";

    private String tokenName;

    private PrivilegeService privilegeService;

    private RedisUtil redisUtil;

    private Integer jwtExpireTime = 3600;

    private Integer refreshJwtTime = 60;

    public AuthFilter(Config config) {
        this.tokenName = config.getTokenName();
    }

    public void setPrivilegeService(PrivilegeService privilegeService) {
        this.privilegeService = privilegeService;
    }

    public void setRedisUtil(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    public void setJwtExpireTime(Integer jwtExpireTime) {
        this.jwtExpireTime = jwtExpireTime;
    }

    public void setRefreshJwtTime(Integer refreshJwtTime) {
        this.refreshJwtTime = refreshJwtTime;
    }

    /**
     * gateway001 权限过滤器
     * 1. 检查JWT是否合法,以及是否过期，如果过期则需要在response的头里换发新JWT，如果不过期将旧的JWT在response的头中返回
     * 2. 判断用户的shopid是否与路径上的shopid一致（0可以不做这一检查）
     * 3. 在redis中判断用户是否有权限访问url,如果不在redis中需要通过dubbo接口load用户权限
     * 4. 需要以微服务接口访问privilegeservice
     *
     * @param exchange
     * @param chain
     * @return
     * @author wwc
     * @date 2020/12/02 17:13
     */
    /**
     * 将判断token是否被ban的逻辑用lua脚本重写
     * @author Jianjian Chan
     * @date 2021/12/03
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        // 获取请求参数
        String token = request.getHeaders().getFirst(tokenName);
        RequestPath url = request.getPath();
        HttpMethod method = request.getMethod();
        // 判断token是否为空，无需token的url在配置文件中设置
        logger.debug("filter: token = " + token);
        if (StringUtil.isNullOrEmpty(token)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.writeWith(Mono.empty());
        }
        // 判断token是否合法
        JwtHelper.UserAndDepart userAndDepart = new JwtHelper().verifyTokenAndGetClaims(token);
        if (userAndDepart == null) {
            // 若token解析不合法
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.writeWith(Mono.empty());
        } else {
            // 若token合法
            // 判断该token是否被ban
            String[] banSetNames = {"BanJwt_0", "BanJwt_1"};
            String scriptPath = "check-jwt.lua";

            DefaultRedisScript<Boolean> script = new DefaultRedisScript<>();

            script.setScriptSource(new ResourceScriptSource(new ClassPathResource(scriptPath)));
            script.setResultType(Boolean.class);

            List<String> keyList = List.of(banSetNames);

            Boolean baned = redisUtil.executeScript(script, keyList, token);

            if(baned) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.writeWith(Mono.empty());
            }
/*
            String[] banSetName = {"BanJwt_0", "BanJwt_1"};
            for (String singleBanSetName : banSetName) {
                // 若redis有该banSetname键则检查
                if (redisUtil.hasKey(singleBanSetName)) {
                    // 获取全部被ban的jwt,若banjwt中有该token则拦截该请求
                    if (redisUtil.isMemberSet(singleBanSetName, token)) {
                        response.setStatusCode(HttpStatus.UNAUTHORIZED);
                        return response.writeWith(Mono.empty());
                    }
                }
            }
 */
            // 检测完了则该token有效
            // 解析userid和departid和有效期
            Long userId = userAndDepart.getUserId();
            Long departId = userAndDepart.getDepartId();
            String userName = userAndDepart.getUserName();
            Date expireTime = userAndDepart.getExpTime();
            Integer userLevel = userAndDepart.getUserLevel();
            // 检验api中传入token是否和departId一致
            if (url != null) {
                // 获取路径中的shopId
                Map<String, String> uriVariables = exchange.getAttribute(ServerWebExchangeUtils.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
                ;
                String pathId = uriVariables.get("did");
                if (pathId != null && !departId.equals(0L)) {
                    // 若非空且解析出的部门id非0则检查是否匹配
                    if (!pathId.equals(departId.toString())) {
                        // 若id不匹配
                        logger.debug("did不匹配:" + pathId);
                        response.setStatusCode(HttpStatus.FORBIDDEN);
                        return response.writeWith(Mono.empty());
                    }
                }
                logger.debug("did匹配");
            } else {
                logger.debug("请求url为空");
                response.setStatusCode(HttpStatus.BAD_REQUEST);
                return response.writeWith(Mono.empty());
            }

            String jwt = token;
            // 判断redis中是否存在该用户的token，若不存在则重新load用户的权限
            String key = String.format(USERKEY, userId);
            if (!redisUtil.hasKey(key)) {
                // 如果redis中没有该键值
                // 通过内部调用将权限载入redis并返回新的token
                privilegeService.loadUserPriv(userId);
            }
            // 将token放在返回消息头中
            response.getHeaders().set(tokenName, jwt);
            // 将url中的数字替换成{id}
            Pattern p = Pattern.compile("/(0|[1-9][0-9]*)");
            Matcher matcher = p.matcher(url.toString());
            String commonUrl = matcher.replaceAll("/{id}");
            logger.debug("获取通用请求路径:" + commonUrl);
            // 找到该url所需要的权限id
            Integer requestType = RequestType.getCodeByType(method).getCode();
            String urlKey = String.format(PRIVKEY, commonUrl, requestType);
            if (!redisUtil.hasKey(urlKey)){
                RequestVo vo = new RequestVo(commonUrl, requestType);
                privilegeService.loadPrivilege(vo);
            }
            Long privId = (Long) redisUtil.get(urlKey);
            boolean next = false;
            if (privId == null) {
                // 若该url无对应权限id
                logger.debug("该url无权限id:" + urlKey);
                next = true;
            }

            // 拿到该用户的权限位,检验是否具有访问该url的权限
            if (redisUtil.isMemberSet(key, privId)) {
                next = true;
            }

            if (next) {
                return chain.filter(exchange).then(
                        Mono.fromRunnable(() -> {
                                    // 判断该token有效期是否还长，load用户权限需要传token，将要过期的旧的token暂未放入banjwt中，有重复登录的问题
                                    Long sec = expireTime.getTime() - System.currentTimeMillis();
                                    if (sec < refreshJwtTime * 1000) {
                                        // 若快要过期了则重新换发token 创建新的token
                                        JwtHelper jwtHelper = new JwtHelper();
                                        String newJwt = jwtHelper.createToken(userId, userName, departId, userLevel, jwtExpireTime);
                                        logger.debug("重新换发token:" + jwt);
                                        response.getHeaders().set(tokenName, newJwt);
                                    }
                                }
                        )
                );
            }
            // 若全部检查完则无该url权限
            logger.debug("无权限");
            // 设置返回消息
            JSONObject message = new JSONObject();
            message.put("errno", ReturnNo.AUTH_NO_RIGHT);
            message.put("errmsg", "无权限访问该url");
            byte[] bits = message.toJSONString().getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = response.bufferFactory().wrap(bits);
            //指定编码，否则在浏览器中会中文乱码
            response.getHeaders().add("Content-Type", "text/plain;charset=UTF-8");
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return response.writeWith(Mono.just(buffer));
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    public static class Config {
        private String tokenName;

        public Config() {

        }

        public String getTokenName() {
            return tokenName;
        }

        public void setTokenName(String tokenName) {
            this.tokenName = tokenName;
        }
    }

    /**
     * 请求类型
     */
    public enum RequestType {
        GET(0, "GET"),
        POST(1, "POST"),
        PUT(2, "PUT"),
        DELETE(3, "DELETE");

        private static final Map<Integer, RequestType> typeMap;

        static { //由类加载机制，静态块初始加载对应的枚举属性到map中，而不用每次取属性时，遍历一次所有枚举值
            typeMap = new HashMap();
            for (RequestType enum1 : values()) {
                typeMap.put(enum1.code, enum1);
            }
        }

        private int code;
        private String description;

        RequestType(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public static RequestType getTypeByCode(Integer code) {
            return typeMap.get(code);
        }

        public static RequestType getCodeByType(HttpMethod method) {
            switch (method) {
                case GET: return RequestType.GET;
                case PUT: return RequestType.PUT;
                case POST: return RequestType.POST;
                case DELETE: return RequestType.DELETE;
                default: return null;
            }
        }

        public Integer getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

    }
}
