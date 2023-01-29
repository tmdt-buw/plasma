package de.buw.tmdt.plasma.services.kgs.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Ontology {

    public static final String LABEL_PROPERTY = "label";
    public static final String FILENAME_PROPERTY = "filename";
    public static final String PREFIX_PROPERTY = "prefix";
    public static final String URI_PROPERTY = "uri";
    public static final String DESCRIPTION_PROPERTY = "description";
    public static final String LOCAL_PROPERTY = "local";

    private String label;

    private String filename;

    private String prefix;

    private String uri;

    private String description;

    private boolean local = false;

    public Ontology(String label, String filename, String prefix, String uri) {
        this.label = label;
        this.filename = filename;
        this.prefix = prefix;
        this.uri = uri;
    }

    @JsonCreator
    public Ontology(
            @JsonProperty(LABEL_PROPERTY) String label,
            @JsonProperty(FILENAME_PROPERTY) String filename,
            @JsonProperty(PREFIX_PROPERTY) String prefix,
            @JsonProperty(URI_PROPERTY) String uri,
            @JsonProperty(DESCRIPTION_PROPERTY) String description,
            @JsonProperty(LOCAL_PROPERTY) boolean local) {
        this.label = label;
        this.filename = filename;
        this.prefix = prefix;
        this.uri = uri;
        this.local = local;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public boolean isLocal() {
        return local;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
