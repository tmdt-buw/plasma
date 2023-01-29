package de.buw.tmdt.plasma.services.kgs.shared.model;

public class OntologyInfo {

    private final String prefix;

    private final String uri;

    private final String label;

    private boolean local = false;
    private final String description;
    private int noProperties;
    private int noClasses;

    public OntologyInfo(String label, String prefix, String uri) {
        this.prefix = prefix;
        this.uri = uri;
        this.label = label;
        this.description = null;
    }

    public OntologyInfo(String label, String prefix, String uri, boolean local, String description) {
        this.prefix = prefix;
        this.uri = uri;
        this.label = label;
        this.local = local;
        this.description = description;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getUri() {
        return uri;
    }

    public String getLabel() {
        return label;
    }

    public boolean isLocal() {
        return local;
    }

    public String getDescription() {
        return description;
    }

    public void setPropertiesCount(int size) {
        this.noProperties = size;
    }

    public void setClassesCount(int size) {
        this.noClasses = size;
    }

    public int getNoProperties() {
        return noProperties;
    }

    public int getNoClasses() {
        return noClasses;
    }
}
