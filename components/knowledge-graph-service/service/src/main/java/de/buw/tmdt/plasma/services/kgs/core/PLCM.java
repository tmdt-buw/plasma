package de.buw.tmdt.plasma.services.kgs.core;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class PLCM {

    public static final String uri = "http://plasma.uni-wuppertal.de/cm#";
    public static final Property xCoordinate = Init.xCoordinate();
    public static final Property yCoordinate = Init.yCoordinate();
    public static final Property originalLabel = Init.originalLabel();
    public static final Property syntaxNodeId = Init.syntaxNodeId();
    public static final Property syntaxNodePath = Init.syntaxNodePath();
    public static final Property cmUuid = Init.cmUuid();
    public static final Property hasNode = Init.hasNode();
    public static final Property label = Init.label();
    public static final Property description = Init.description();
    public static final Resource SemanticModel = Init.semanticModel();
    public static final Resource NamedEntity = Init.namedEntity();

    protected static final Resource resource(String local) {
        return ResourceFactory.createResource(uri + local);
    }

    protected static final Property property(String local) {
        return ResourceFactory.createProperty(uri, local);
    }

    private PLCM() {

    }

    public static class Init {
        public static Property xCoordinate() {
            return property("x");
        }

        public static Property yCoordinate() {
            return property("y");
        }

        public static Property originalLabel() {
            return property("originalLabel");
        }

        public static Property syntaxNodeId() {
            return property("syntaxNodeId");
        }

        public static Property syntaxNodePath() {
            return property("syntaxNodePath");
        }

        public static Property cmUuid() {
            return property("cmUuid");
        }

        public static Property hasNode() {
            return property("hasNode");
        }

        public static Property label() {
            return property("label");
        }

        public static Property description() {
            return property("description");
        }

        public static Resource semanticModel() {
            return resource("SemanticModel");
        }

        public static Resource namedEntity() {
            return resource("NamedEntity");
        }

    }

    /**
     * Returns the URI for this schema.
     *
     * @return the URI for this schema
     */
    public static String getURI() {
        return uri;
    }
}
