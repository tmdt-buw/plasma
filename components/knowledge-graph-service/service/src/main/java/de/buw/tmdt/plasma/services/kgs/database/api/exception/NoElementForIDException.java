package de.buw.tmdt.plasma.services.kgs.database.api.exception;

import org.jetbrains.annotations.NotNull;

public class NoElementForIDException extends GraphDBException {

	private static final long serialVersionUID = 1252803218876335261L;

	/**
	 * CAUTION: this constructor does not have the expected default {@code (String message)} behaviour but injects the {@code id} into a message template.
	 *
	 * @param id the id of the requested vertex
	 */
	public NoElementForIDException(@NotNull String id) {
		super(String.format("Failed to retrieve vertex with ID '%s'", id));
	}
}