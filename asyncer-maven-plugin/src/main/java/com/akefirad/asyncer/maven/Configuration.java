package com.akefirad.asyncer.maven;

import lombok.Data;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;

@UtilityClass
@SuppressWarnings("WeakerAccess")
public class Configuration {

    @Data
    public static class Paths {
        private File source;
        private File destination;
        private String[] includes = ArrayUtils.toArray("*.java");
        private String[] excludes = new String[0];
        private File[] dependencies = new File[0];
    }

}
