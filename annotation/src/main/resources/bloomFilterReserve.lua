local bloomName = KEYS[1]
local error_rate = KEYS[2]
local capacity = KEYS[3]

-- bloomFilter
local result_1 = redis.call('BF.RESERVE', bloomName, error_rate, capacity)
return result_1