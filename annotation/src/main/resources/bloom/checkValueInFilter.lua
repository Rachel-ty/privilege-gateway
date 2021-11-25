--作用：检查value是否在filter中

local filterName = KEYS[1]
local value = KEYS[2]

--返回值：整数
--1：filter中可能存在value
--0：filter中不存在value
local result = redis.call('BF.EXISTS', filterName, value)
return result