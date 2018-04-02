package org.springframework.scheduling.annotation;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class AsyncResult<T> implements Future<T> {
    public AsyncResult(T object) {
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public T get() {
        return null;
    }

    @Override
    public T get(long timeout, TimeUnit unit) {
        return null;
    }
}
