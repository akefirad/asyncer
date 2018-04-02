package com.akefirad.test;

import java.util.List;

public abstract class ClassWithMethodReturningListOfSubTypeOfObjects {

    public abstract <T extends Object> List<T> methodReturningListOfSubTypeOfObjects();

}