--作用：向filter中插入value

local filterName = KEYS[1]
local value = KEYS[2]

--返回值：整数
--1：向filter中插入value成功
--0：插入失败
local result = redis.call('BF.ADD', filterName, value)
return result