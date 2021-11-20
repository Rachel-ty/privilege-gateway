local filterName = KEYS[1]

local result = redis.call("EXISTS", filterName);
return result