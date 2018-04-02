package com.akefirad.maven;

import com.akefirad.asyncer.api.Logger;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.maven.plugin.logging.Log;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class MavenLogger implements Logger, Log {

    @NonNull
    private final Supplier<Log> supplier;

    private Log getLog() {
        return supplier.get();
    }

    @Override
    public boolean isDebugEnabled() {
        return getLog().isDebugEnabled();
    }

    @Override
    public void debug(CharSequence content) {
        getLog().debug(content);
    }

    @Override
    public void debug(CharSequence content, Throwable error) {
        getLog().debug(content, error);
    }

    @Override
    public void debug(Throwable error) {
        getLog().debug(error);
    }

    @Override
    public void debug(String format, Object... args) {
        if (getLog().isDebugEnabled()) {
            getLog().debug(String.format(format, args));
        }
    }

    @Override
    public boolean isInfoEnabled() {
        return getLog().isInfoEnabled();
    }

    @Override
    public void info(CharSequence content) {
        getLog().info(content);
    }

    @Override
    public void info(CharSequence content, Throwable error) {
        getLog().info(content, error);
    }

    @Override
    public void info(Throwable error) {
        getLog().info(error);
    }

    @Override
    public void info(String format, Object... args) {
        if (getLog().isInfoEnabled()) {
            getLog().info(String.format(format, args));
        }
    }

    @Override
    public boolean isWarnEnabled() {
        return getLog().isWarnEnabled();
    }

    @Override
    public void warn(CharSequence content) {
        getLog().warn(content);
    }

    @Override
    public void warn(CharSequence content, Throwable error) {
        getLog().warn(content, error);
    }

    @Override
    public void warn(Throwable error) {
        getLog().warn(error);
    }

    @Override
    public void warn(String format, Object... args) {
        if (getLog().isWarnEnabled()) {
            getLog().warn(String.format(format, args));
        }
    }

    @Override
    public boolean isErrorEnabled() {
        return getLog().isErrorEnabled();
    }

    @Override
    public void error(CharSequence content) {
        getLog().error(content);
    }

    @Override
    public void error(CharSequence content, Throwable error) {
        getLog().error(content, error);
    }

    @Override
    public void error(Throwable error) {
        getLog().error(error);
    }

    @Override
    public void error(String format, Object... args) {
        if (getLog().isErrorEnabled()) {
            getLog().error(String.format(format, args));
        }
    }

}
