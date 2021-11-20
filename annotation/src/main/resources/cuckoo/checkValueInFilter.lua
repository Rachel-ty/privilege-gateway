local filterName = KEYS[1]
local key = KEYS[2]

local result = redis.call('CF.EXISTS', filterName, key)
return result