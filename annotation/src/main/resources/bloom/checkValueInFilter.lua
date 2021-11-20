local filterName = KEYS[1]
local key = KEYS[2]

local result = redis.call('BF.EXISTS', filterName, key)
return result