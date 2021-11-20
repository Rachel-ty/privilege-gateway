local filterName = KEYS[1]
local key = KEYS[2]

local result = redis.call('CF.ADD', filterName, key)
return result