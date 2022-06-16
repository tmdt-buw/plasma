package de.buw.tmdt.plasma.datamodel.semanticmodel;

public interface NodeTemplate {
    NodeTemplate convertToTemplate();

    boolean isTemplate();

    /**
     * Generate a new entity from this one.
     *
     * @return A new instance of the same type, all fields but instance-related ones (like 'uuid') are copied
     */
    NodeTemplate instanciate();
}
