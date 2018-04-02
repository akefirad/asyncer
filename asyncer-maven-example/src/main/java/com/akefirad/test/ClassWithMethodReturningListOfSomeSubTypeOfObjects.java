package com.akefirad.test;

import java.util.List;

public abstract class ClassWithMethodReturningListOfSomeSubTypeOfObjects {

    public abstract List<? extends Object> methodReturningListOfSomeSubTypeOfObjects();

}