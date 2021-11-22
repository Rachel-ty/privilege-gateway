package cn.edu.xmu.privilegegateway.util.coder.imp;

import cn.edu.xmu.privilegegateway.util.coder.Sign;
import cn.edu.xmu.privilegegateway.util.encript.SHA256;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;

@NoArgsConstructor
public class SHA256Sign extends Sign {
    public SHA256Sign(String... fieldNames) {
        fieldNameList = new ArrayList<>();
        fieldNameList.addAll(Arrays.asList(fieldNames));
    }

    @Override
    public String encrypt(String content) {
        return SHA256.getSHA256(content);
    }
}
