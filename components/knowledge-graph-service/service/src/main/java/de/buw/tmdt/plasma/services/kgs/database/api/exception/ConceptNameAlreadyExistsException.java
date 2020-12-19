package de.buw.tmdt.plasma.services.kgs.database.api.exception;

import org.jetbrains.annotations.NotNull;

public class ConceptNameAlreadyExistsException extends GraphDBException {
	private static final String MESSAGE_TEMPLATE = "Concept with unique name \"%s\" already exists.";
	private static final long serialVersionUID = 6613737068753678368L;

	public ConceptNameAlreadyExistsException(@NotNull String name) {
		super(String.format(MESSAGE_TEMPLATE, name));
	}

}
