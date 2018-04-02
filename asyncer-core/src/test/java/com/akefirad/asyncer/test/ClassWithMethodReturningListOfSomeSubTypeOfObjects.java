package com.akefirad.asyncer.test;

import java.util.List;

public abstract class ClassWithMethodReturningListOfSomeSubTypeOfObjects {

    public abstract List<? extends Object> methodReturningListOfSomeSubTypeOfObjects();

}