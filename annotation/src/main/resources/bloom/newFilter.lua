--作用：新建指定参数的filter

local filterName = KEYS[1]
local errorRate = KEYS[2]
local capacity = KEYS[3]

--返回值：只有1项的表
--["ok":"OK"] 新建成功
--["err":"{err_msg}"] 新建失败
local result = redis.pcall("BF.RESERVE", filterName, errorRate, capacity) --采用call会抛异常，采用pcall会返回错误信息
for key, value in pairs(result) do
    if key == 'ok' then
        return true
    else
        return false
    end
end
