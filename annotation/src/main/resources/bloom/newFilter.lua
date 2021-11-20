local filterName = KEYS[1]
local errorRate = KEYS[2]
local capacity = KEYS[3]

local result = redis.call("BF.RESERVE", filterName, errorRate, capacity)
if result == "OK" then
    return 1
else
    return 0
end
