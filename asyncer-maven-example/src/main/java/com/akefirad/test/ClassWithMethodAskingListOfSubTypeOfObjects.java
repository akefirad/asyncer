package com.akefirad.test;

import java.util.List;

public abstract class ClassWithMethodAskingListOfSubTypeOfObjects {

    public abstract <T extends Object> void methodAskingListOfSubTypeOfObjects(List<T> objects);

}