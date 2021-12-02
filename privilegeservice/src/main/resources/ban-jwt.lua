--KEYS（为key的参数）:
--所有BanSet的名称
--BanIndex的名称
--ARGV（不为key的参数）:
--BanSet的数量
--要加入BanSet的jwt
--BanSet的过期时间

local setCounts = tonumber(ARGV[1])
local jwt = ARGV[2]
local expireTime = tonumber(ARGV[3])

local banIndexKey = KEYS[setCounts + 1]
local banIndexInRedis = 0;

if(0 == redis.call('EXISTS', banIndexKey)) then
    redis.call('SET', banIndexKey, banIndexInRedis)
else
    banIndexInRedis = tonumber(redis.call('GET', banIndexKey))
end

local setName = KEYS[banIndexInRedis % setCounts + 1]

if(0 == redis.call('EXISTS', setName)) then
    redis.call('SADD', setName, jwt)
    redis.call('EXPIRE', setName, 2 * expireTime)
else
    local timeToLive = tonumber(redis.call('TTL', setName))
    if(timeToLive > expireTime) then
        redis.call('SADD', setName, jwt)
    else
        redis.call('INCR', banIndexKey)
        banIndexInRedis = tonumber(redis.call('GET', banIndexKey))

        setName = KEYS[banIndexInRedis % setCounts + 1]

        redis.call('DEL', setName)
        redis.call('SADD', setName, jwt)
        redis.call('EXPIRE', setName, expireTime)
    end
end