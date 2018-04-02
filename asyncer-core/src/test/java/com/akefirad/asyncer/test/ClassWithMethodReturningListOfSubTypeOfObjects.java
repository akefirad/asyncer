package com.akefirad.asyncer.test;

import java.util.List;

public abstract class ClassWithMethodReturningListOfSubTypeOfObjects {

    public abstract <T extends Object> List<T> methodReturningListOfSubTypeOfObjects();

}