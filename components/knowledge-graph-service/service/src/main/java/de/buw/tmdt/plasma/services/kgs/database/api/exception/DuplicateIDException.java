package de.buw.tmdt.plasma.services.kgs.database.api.exception;

import org.jetbrains.annotations.NotNull;

public class DuplicateIDException extends GraphDBException {
	private static final String MESSAGE_TEMPLATE = "ID \"%s\" is not unique.";
	private static final long serialVersionUID = -8341009049237598410L;
	private final String id;

	public DuplicateIDException(@NotNull String id) {
		super(String.format(MESSAGE_TEMPLATE, id));
		this.id = id;
	}

	@NotNull
	public String getId() {
		return id;
	}
}
