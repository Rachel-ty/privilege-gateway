local filterName = KEYS[1]

local result = redis.call('DEL', filterName)
return result