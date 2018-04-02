package com.akefirad.asyncer.test;

public abstract class ClassWithMethodReturningInnerType {

    public abstract Foo methodReturningInnerType();

    public static class Foo {
    }

}