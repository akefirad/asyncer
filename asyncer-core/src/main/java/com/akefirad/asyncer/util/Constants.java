package com.akefirad.asyncer.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {

    @UtilityClass
    public static class Parameters {
        public static final String GENERATED_PACKAGE_NAME = "patterns.generatedPackageName";
        public static final String GENERATED_TYPE_NAME = "patterns.generatedTypeName";
        public static final String GENERATED_METHOD_NAME = "patterns.generatedMethodName";

        public static class Spring {
            public static final String BEAN_TYPE = "spring.beanType";
            public static final String BEAN_NAME = "spring.beanName";
            public static final String ASYNC_TYPE = "spring.asyncType";
            public static final String IS_LAZY_BEAN = "spring.isLazyBean";
            public static final String EXECUTOR_NAME = "spring.executorName";
            public static final String RETURN_TYPE = "spring.returnType";
        }
    }

    @UtilityClass
    public static class Variables {
        public static final String ORIGINAL_PACKAGE_NAME = "original.package.name";
        public static final String ORIGINAL_TYPE_NAME = "original.type.name";
        public static final String ORIGINAL_METHOD_NAME = "original.method.name";
        public static final String GENERATED_PACKAGE_NAME = "generated.package.name";
        public static final String GENERATED_TYPE_NAME = "generated.type.name";
        public static final String GENERATED_METHOD_NAME = "generated.method.name";
    }

    @UtilityClass
    public static class Placeholders {
        public static final String ORIGINAL_PACKAGE_NAME = "#{" + Variables.ORIGINAL_PACKAGE_NAME + "}";
        public static final String ORIGINAL_TYPE_NAME = "#{" + Variables.ORIGINAL_TYPE_NAME + "}";
        public static final String ORIGINAL_METHOD_NAME = "#{" + Variables.ORIGINAL_METHOD_NAME + "}";
        public static final String GENERATED_PACKAGE_NAME = "#{" + Variables.GENERATED_PACKAGE_NAME + "}";
        public static final String GENERATED_TYPE_NAME = "#{" + Variables.GENERATED_TYPE_NAME + "}";
        public static final String GENERATED_METHOD_NAME = "#{" + Variables.GENERATED_METHOD_NAME + "}";
    }

}
