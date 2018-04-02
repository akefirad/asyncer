package com.akefirad.asyncer.spring;

import com.akefirad.asyncer.api.Asyncer;
import com.akefirad.asyncer.api.Logger;
import com.akefirad.asyncer.core.JavaSourceParser;
import com.akefirad.asyncer.util.Constants;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

class SpringMethodAsyncerTest {

    private final Asyncer asyncer = new SpringAsyncer(new JavaSourceParser(new Logger.StdOutLogger(), singletonList(new File("src/test/java"))));

    @Test
    @DisplayName("Class with abstract method should create Async method")
    void testClassWithAbstractMethod() throws IOException {
        // given
        String className = "com/akefirad/asyncer/test/ClassWithMethodAskingListOfListOfObjects";
        ImmutableMap<String, String> options = ImmutableMap.<String, String>builder()
                .put(Constants.Parameters.GENERATED_PACKAGE_NAME, "#{original.package.name}.async")
                .put(Constants.Parameters.GENERATED_TYPE_NAME, "Async#{original.type.name}")
                .put(Constants.Parameters.Spring.IS_LAZY_BEAN, "yes")
                .build();

        // when
        Optional<String> async = asyncer.make(readSourceCodeOfJavaClass(className), options);

        // then
        System.out.println(async.orElse(""));
    }

    @Test
    void testAllClasses() throws IOException {
        // given
        List<File> files = asList(listAllTestClassesIn("src/test/java/com/akefirad/asyncer/test"));
        files.sort(Comparator.comparing(File::getName));
        ImmutableMap<String, String> options = ImmutableMap.<String, String>builder()
                .put(Constants.Parameters.GENERATED_PACKAGE_NAME, "#{original.package.name}.async")
                .put(Constants.Parameters.GENERATED_TYPE_NAME, "Async#{original.type.name}")
                .put(Constants.Parameters.GENERATED_METHOD_NAME, "#{original.method.name}Async")
                .put(Constants.Parameters.Spring.IS_LAZY_BEAN, "true")
                .put(Constants.Parameters.Spring.BEAN_TYPE, SpringAsyncer.SERVICE_ANNOTATION)
                .put(Constants.Parameters.Spring.BEAN_NAME, Constants.Placeholders.ORIGINAL_PACKAGE_NAME + "." + Constants.Placeholders.ORIGINAL_TYPE_NAME)
                .put(Constants.Parameters.Spring.ASYNC_TYPE, SpringAsyncer.SPRING_ASYNC_METHOD)
                .put(Constants.Parameters.Spring.EXECUTOR_NAME, Constants.Placeholders.ORIGINAL_METHOD_NAME)
                .build();

        // when
        for (File file : files) {
            Optional<String> async = asyncer.make(new String(Files.readAllBytes(file.toPath()), UTF_8), options);
            System.out.println(async.orElse("NOTHING"));
            System.out.println("-------------------------------------------------------------------");
        }

        // then
        System.out.println("done");
    }

    private String readSourceCodeOfJavaClass(String filename) throws IOException {
        String path = "src/test/java/" + filename + ".java";
        return new String(Files.readAllBytes(Paths.get(path)), UTF_8);
    }

    private File[] listAllTestClassesIn(String directory) {
        return new File(directory).listFiles((dir, name) -> name.endsWith(".java"));
    }
}