package com.akefirad.asyncer.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.text.StrLookup;
import org.apache.commons.lang3.text.StrSubstitutor;

import java.io.File;

import static com.google.common.base.Preconditions.checkArgument;

@UtilityClass
public class JavaUtils {

    public static StrSubstitutor newStrSubstitutor(@NonNull StrLookup<?> resolver) {
        return new StrSubstitutor(resolver, "#{", "}", '#');
    }

    public static File requireDirectory(@NonNull File file) {
        checkArgument(file.isDirectory(), "Not a directory: %s", file);
        return file;
    }

}
