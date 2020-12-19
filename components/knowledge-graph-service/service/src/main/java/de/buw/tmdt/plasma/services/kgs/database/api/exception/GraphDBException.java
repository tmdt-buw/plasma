package de.buw.tmdt.plasma.services.kgs.database.api.exception;

import org.jetbrains.annotations.NotNull;

public class GraphDBException extends RuntimeException {

	private static final long serialVersionUID = -3842701874862275388L;

	public GraphDBException(@NotNull String message) {
		super(message);
	}
}
