package com.akefirad.asyncer.test;

import java.util.Map;

public abstract class ClassWithMethodReturningTwoTypes {

    public abstract <S, T> Map<S, T> methodReturningT();

}