---
---
local key=KEYS[1]
local value=ARGS[1]
local result=redis.call("srem",key,value)
return result
