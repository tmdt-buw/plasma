package de.buw.tmdt.plasma.utilities.misc;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface Logging {
    default @NotNull Logger getLogger() {
        return LoggerFactory.getLogger(this.getClass());
    }
}
