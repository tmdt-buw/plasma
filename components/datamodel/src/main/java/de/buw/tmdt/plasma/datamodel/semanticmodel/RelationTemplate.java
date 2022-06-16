package de.buw.tmdt.plasma.datamodel.semanticmodel;

public interface RelationTemplate {

    RelationTemplate convertToTemplate();

    boolean isTemplate();

    /**
     * Generate a new entity from this one.
     * Will not copy fields like 'fromId' and 'toId' which have to be defined with this call.
     *
     * @param fromId The new from id to set
     * @param toId   The new to id to set
     * @return A new instance of the same type, all fields but instance-related ones (like 'uuid') are copied.
     */
    RelationTemplate instanciate(String toId, String fromId);
}
