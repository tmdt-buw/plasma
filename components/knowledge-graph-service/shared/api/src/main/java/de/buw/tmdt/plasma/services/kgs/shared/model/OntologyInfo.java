package de.buw.tmdt.plasma.services.kgs.shared.model;

public class OntologyInfo {

    private final String prefix;

    private final String uri;

    private final String label;

    private boolean local = false;

    public OntologyInfo(String label, String prefix, String uri) {
        this.prefix = prefix;
        this.uri = uri;
        this.label = label;
    }

    public OntologyInfo(String label, String prefix, String uri, boolean local) {
        this.prefix = prefix;
        this.uri = uri;
        this.label = label;
        this.local = local;
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
}
