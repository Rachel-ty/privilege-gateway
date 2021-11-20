local len = #KEYS
local result
local args1 = KEYS[1]
local args2 = KEYS[2]
local args3 = KEYS[3]
local args4 = KEYS[4]
local args5 = KEYS[5]
local args6 = KEYS[6]
local args7 = KEYS[7]
local args8 = KEYS[8]

if len == 2 then
    result=redis.call("CF.RESERVE", args1,args2)
end
if len == 4 then
    result=redis.call("CF.RESERVE", args1,args2,args3,args4)
end
if len == 6 then
    result=redis.call("CF.RESERVE", args1,args2,args3,args4,args5,args6)
end
if len == 8 then
    result=redis.call("CF.RESERVE", args1,args2,args3,args4,args5,args6,args7,args8)
end

for key,value in pairs(result) do
    if value == "OK" then
        return 1
    else
        return 0
    end
end

