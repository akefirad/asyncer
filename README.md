## Asyncer, makes it asynchronous!
Assume you have the following class, (either source code or compiled class)
```java
package mypackage;

import java.util.List;

public class MyClass {

    public void foo(Object... objects) {
        // Fun stuff
    }

    public Object bar() {
        // Fun stuff
    }

}
```
This library makes it asynchronous in different versions (using pure Java concurrent package or Spring asynchronous feature).
It generates a new class which has the same interface (except the return type of methods) but all methods are asynchronous. The following is the Spring version of the above class: 
```java
package mypackage.async;

import java.util.List;

@Lazy // optional!
@Component("AsyncMyClass") // or @Service, also bean name is optional!
public class AsyncMyClass {

    private final MyClass delegate;
    
    public AsyncMyClass(MyClass delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate")
    }

    @Async("myTaskExecutor") // can be class level!
    public void foo(Object... objects) {
        this.delegate.foo(objects);
        // Also it can return a future object to caller!
        //return CompletableFuture.completedFuture(null);
    }

    @Async("myTaskExecutor") // executor name is optional!
    public Futue<Object> bar(Object... objects) { // or ComputableFuture<Object>
        return new AsyncResult<>(this.delegate.bar());
        // Or
        //return CompletableFuture.completedFuture(this.delegate.bar());
    }

}
```
TBC