package de.buw.tmdt.plasma.services.kgs.database.neo4j;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;

public class Neo4JModel {

    public abstract static class Neo4JEntity implements Serializable {
        private static final long serialVersionUID = -8139876611079449611L;

        protected Neo4JEntity(@NotNull Neo4JDatabaseInitializer neo4JDatabaseInitializer) {
            neo4JDatabaseInitializer.registerEntity(this);
        }

        @NotNull
        abstract String getClassProperty();

        @NotNull
        abstract String getIndexProperty();
    }

    @Component
    public static final class EntityConcept extends Neo4JEntity {
        public static final String CLASS = "EntityConcept";
        public static final String ID_PREFIX = "ec";
        public static final String PROPERTY_ID = "ID";
        public static final String PROPERTY_DESCRIPTION = "description";
        public static final String PROPERTY_SOURCE_URI = "sourceURI"; //the URI source from which this concept was obtained
        public static final String PROPERTY_MAIN_LABEL = "mainLabel";
        private static final long serialVersionUID = -3501193779208749334L;

        @Autowired
        private EntityConcept(@NotNull Neo4JDatabaseInitializer neo4JDatabaseInitializer) {
            super(neo4JDatabaseInitializer);
        }

        @Override
        @NotNull String getClassProperty() {
            return CLASS;
        }

        @Override
        @NotNull String getIndexProperty() {
            return PROPERTY_ID;
        }

        public static final class Edge {
            public static final String RELATED = "related";
            public static final String SYNONYM = "synonym";

            private Edge() {
            }
        }
    }

    @Component
    public static final class RelationConcept extends Neo4JEntity {
        public static final String CLASS = "RelationConcept";
        public static final String ID_PREFIX = "rc";
        public static final String PROPERTY_ID = "ID";
        public static final String PROPERTY_LABEL = "label";
        public static final String PROPERTY_DESCRIPTION = "description";
        public static final String PROPERTY_SOURCE_URI = "sourceURI"; //the URI source from which this concept was obtained;
        private static final long serialVersionUID = -6195400747237751596L;

        @Autowired
        private RelationConcept(@NotNull Neo4JDatabaseInitializer neo4JDatabaseInitializer) {
            super(neo4JDatabaseInitializer);
        }

        @Override
        @NotNull String getClassProperty() {
            return CLASS;
        }

        @Override
        @NotNull String getIndexProperty() {
            return PROPERTY_ID;
        }
    }

    @Component
    public static final class EntityConceptRelation extends Neo4JEntity {
        public static final String CLASS = "EntityConceptRelation";
        public static final String ID_PREFIX = "ecr";
        public static final String PROPERTY_ID = "ID";
        public static final String PROPERTY_USAGE_ABSOLUTE = "absoluteNumberOfUsages";
        private static final long serialVersionUID = -7663915940954088636L;

        @Autowired
        private EntityConceptRelation(@NotNull Neo4JDatabaseInitializer neo4JDatabaseInitializer) {
            super(neo4JDatabaseInitializer);
        }

        @Override
        @NotNull String getClassProperty() {
            return CLASS;
        }

        @Override
        @NotNull String getIndexProperty() {
            return PROPERTY_ID;
        }
    }

    @Component
    public static final class SemanticModel extends Neo4JEntity {
        public static final String CLASS = "SemanticModel";
        public static final String ID_PREFIX = "sm";
        public static final String PROPERTY_ID = "ID";
        public static final String PROPERTY_LABEL = "label";
        public static final String PROPERTY_DESCRIPTION = "description";
        public static final String PROPERTY_DATA_SOURCE_ID = "dataSourceId";
        private static final long serialVersionUID = 424268131910559892L;

        @Autowired
        private SemanticModel(@NotNull Neo4JDatabaseInitializer neo4JDatabaseInitializer) {
            super(neo4JDatabaseInitializer);
        }

        @Override
        @NotNull String getClassProperty() {
            return CLASS;
        }

        @Override
        @NotNull String getIndexProperty() {
            return PROPERTY_ID;
        }
    }

    @Component
    public static final class EntityType extends Neo4JEntity {
        public static final String CLASS = "EntityType";
        public static final String ID_PREFIX = "et";
        public static final String PROPERTY_ID = "ID";
        public static final String PROPERTY_LABEL = "label";
        public static final String PROPERTY_DESCRIPTION = "description";
        public static final String PROPERTY_MAPPED_TO_DATA = "mappedToData";
        public static final String PROPERTY_ORIGINAL_LABEL = "originalLabel";
        private static final long serialVersionUID = -1530173316133777493L;

        @Autowired
        private EntityType(@NotNull Neo4JDatabaseInitializer neo4JDatabaseInitializer) {
            super(neo4JDatabaseInitializer);
        }

        @Override
        @NotNull String getClassProperty() {
            return CLASS;
        }

        @Override
        @NotNull String getIndexProperty() {
            return PROPERTY_ID;
        }
    }

    @Component
    public static final class Relation extends Neo4JEntity {
        public static final String CLASS = "Relation";
        public static final String ID_PREFIX = "r";
        public static final String PROPERTY_ID = "ID";
        private static final long serialVersionUID = 505091775013236990L;

        @Autowired
        private Relation(@NotNull Neo4JDatabaseInitializer neo4JDatabaseInitializer) {
            super(neo4JDatabaseInitializer);
        }

        @Override
        @NotNull String getClassProperty() {
            return CLASS;
        }

        @Override
        @NotNull String getIndexProperty() {
            return PROPERTY_ID;
        }
    }

    @Component
    public static final class Position extends Neo4JEntity {
        public static final String CLASS = "Position";
        public static final String ID_PREFIX = "pos";
        public static final String PROPERTY_ID = "ID";
        public static final String PROPERTY_X_COORDINATE = "X_COORDINATE";
        public static final String PROPERTY_Y_COORDINATE = "Y_COORDINATE";
        private static final long serialVersionUID = -4747328790383145323L;

        @Autowired
        private Position(@NotNull Neo4JDatabaseInitializer neo4JDatabaseInitializer) {
            super(neo4JDatabaseInitializer);
        }

        @Override
        @NotNull String getClassProperty() {
            return CLASS;
        }

        @Override
        @NotNull String getIndexProperty() {
            return PROPERTY_ID;
        }

        public static final class Edge {
            public static final String POSITION = "position";

            private Edge() {
            }
        }
    }

    @Component
    public static final class PrefixNode extends Neo4JEntity {
        public static final String CLASS = "PrefixNode";
        public static final String ID_PREFIX = "pre";
        public static final String PROPERTY_ID = "ID";
        public static final String PREFIX = "prefix";
        public static final String MAPPED_URL = "mappedUrl";

        @Autowired
        private PrefixNode(@NotNull Neo4JDatabaseInitializer neo4JDatabaseInitializer) {
            super(neo4JDatabaseInitializer);
        }

        @Override
        @NotNull String getClassProperty() {
            return CLASS;
        }

        @Override
        @NotNull String getIndexProperty() {
            return PROPERTY_ID;
        }
    }

    @Component
    public static final class PrefixRepository extends Neo4JEntity {
        public static final String CLASS = "PrefixRepository";
        public static final String ID_PREFIX = "prerepo";
        public static final String PROPERTY_ID = "ID";

        @Autowired
        private PrefixRepository(@NotNull Neo4JDatabaseInitializer neo4JDatabaseInitializer) {
            super(neo4JDatabaseInitializer);
        }

        @Override
        @NotNull String getClassProperty() {
            return CLASS;
        }

        @Override
        @NotNull String getIndexProperty() {
            return PROPERTY_ID;
        }
    }
}