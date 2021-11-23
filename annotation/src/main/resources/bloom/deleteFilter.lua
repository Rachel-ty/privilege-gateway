--作用：删除存在的filter

local filterName = KEYS[1]

--返回值：整数
--1：删除filter成功
--0：删除filter失败
local result = redis.call('DEL', filterName)
return result