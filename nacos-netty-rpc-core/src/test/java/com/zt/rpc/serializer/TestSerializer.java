package com.zt.rpc.serializer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestSerializer {
    
    @Test
    public void test() {
        Serializer serializer = new DefaultSerializer();
        String str = "123";
        byte[] bytes = serializer.serialize(str);
        String newStr = (String) serializer.deserialize(bytes, String.class);
        System.out.println(newStr);
    }
    
}
