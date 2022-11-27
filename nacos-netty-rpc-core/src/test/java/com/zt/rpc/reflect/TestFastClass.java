package com.zt.rpc.reflect;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cglib.reflect.FastClass;

import java.lang.reflect.InvocationTargetException;

@SpringBootTest
public class TestFastClass {

    @Autowired
    private TestPersonService personService;

    @Test
    public void test() throws InvocationTargetException {
        FastClass fastClass = FastClass.create(personService.getClass());
        int methodIdx = fastClass.getIndex("say", new Class[]{});
        fastClass.invoke(methodIdx, personService, new Object[]{});
    }

}
