package cn.edu.xmu.privilegegateway.util;

import org.junit.jupiter.api.Test;

/**
 * @author YuJie 22920192204242
 * @date 2021/11/18
 */
public class TokenTest {


    @Test
    public void testVerifyTokenAndGetClaims() throws Exception{
        JwtHelper jwtHelper=new JwtHelper();
        String token= jwtHelper.createToken(1L,"yujie",1L,1,9600);
        JwtHelper.UserAndDepart uad=jwtHelper.verifyTokenAndGetClaims(token);

        assert uad.getUserId().equals(1L);
        assert uad.getUserName().equals("yujie");
        assert uad.getDepartId().equals(1L);
        assert uad.getUserLevel().equals(1);
    }
}
