package de.buw.tmdt.plasma.services.dms.core.operations.exceptions;

public class ParameterParsingException extends Exception {

	private static final long serialVersionUID = 4749312992544171381L;

	public ParameterParsingException() {
	}

	public ParameterParsingException(String message) {
		super(message);
	}

	public ParameterParsingException(String message, Throwable cause) {
		super(message, cause);
	}

	public ParameterParsingException(Throwable cause) {
		super(cause);
	}

	public ParameterParsingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
