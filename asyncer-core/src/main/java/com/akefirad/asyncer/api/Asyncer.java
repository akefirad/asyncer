package com.akefirad.asyncer.api;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

public interface Asyncer {

    Optional<String> make(String source, Map<String, ?> parameters);

    Optional<File> make(Path source, Path dirDestination, Map<String, ?> parameters) throws IOException;

}
