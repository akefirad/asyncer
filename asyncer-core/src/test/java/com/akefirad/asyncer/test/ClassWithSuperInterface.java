package com.akefirad.asyncer.test;

import com.akefirad.asyncer.util.FooInterface;

public class ClassWithSuperInterface implements FooInterface {

    @Override
    public void foo(String string) {
        System.out.println("foo");
    }

}
