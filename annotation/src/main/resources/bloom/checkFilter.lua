--作用：检查filter是否存在

local filterName = KEYS[1]

--返回值：整数
--1：存在同名的filter
--0：不存在同名的filter
local result = redis.call("EXISTS", filterName);
return result