package de.buw.tmdt.plasma.datamodel.semanticmodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Nullable;

public interface Customizable {

    String INSTANCE_PROPERTY = "instance";

    @JsonProperty(INSTANCE_PROPERTY)
    Instance getInstance();

    void setInstance(@Nullable Instance instance);
}
