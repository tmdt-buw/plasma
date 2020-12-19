package de.buw.tmdt.plasma.services.kgs.database.api.exception;

import org.jetbrains.annotations.NotNull;

public class MissingPropertyException extends GraphDBException {

	private static final long serialVersionUID = 3624384415066627947L;

	/**
	 * CAUTION: this constructor does not have the expected default {@code (String message)} behaviour but injects the {@code propertyName},{@code
	 * String}, {@code elementID} into a message template.
	 *
	 * @param propertyName the name of the property
	 * @param elementType  the type of the property
	 */
	public MissingPropertyException(@NotNull String propertyName, @NotNull String elementType) {
		super(String.format("Property %s was missing in element of Type %s", propertyName, elementType));
	}
}