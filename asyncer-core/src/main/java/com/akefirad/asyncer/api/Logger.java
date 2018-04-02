package com.akefirad.asyncer.api;

import static java.lang.String.format;

public interface Logger {

    void debug(String format, Object... args);

    void info(String format, Object... args);

    void warn(String format, Object... args);

    void error(String format, Object... args);

    class StdOutLogger implements Logger {

        @Override
        public void debug(String format, Object... args) {
            System.out.println("[DEBUG]" + format(format, args));
        }

        @Override
        public void info(String format, Object... args) {
            System.out.println("[INFO]" + format(format, args));
        }

        @Override
        public void warn(String format, Object... args) {
            System.out.println("[WARN]" + format(format, args));
        }

        @Override
        public void error(String format, Object... args) {
            System.out.println("[ERROR]" + format(format, args));
        }
    }
}
