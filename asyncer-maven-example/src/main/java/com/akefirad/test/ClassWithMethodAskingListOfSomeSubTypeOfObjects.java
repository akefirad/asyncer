package com.akefirad.test;

import java.util.List;

public abstract class ClassWithMethodAskingListOfSomeSubTypeOfObjects {

    public abstract void methodAskingListOfSomeSubTypeOfObjects(List<? extends Object> objects);

}