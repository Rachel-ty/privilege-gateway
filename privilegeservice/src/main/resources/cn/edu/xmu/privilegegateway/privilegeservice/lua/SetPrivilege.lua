--- 设置 PRIVILEGE
local key=KEYS[1]
local value=ARGV[1]
local result=redis.call("set",key,value)
return result

