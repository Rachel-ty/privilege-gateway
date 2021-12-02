---通过key,查找对应的value
local key=KEYS[1]
local value=ARGV[1]
local result=redis.call("SISMEMBER",key,value)
return result

