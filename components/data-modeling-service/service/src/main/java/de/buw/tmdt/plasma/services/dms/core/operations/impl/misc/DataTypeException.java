package de.buw.tmdt.plasma.services.dms.core.operations.impl.misc;

public class DataTypeException extends SLTFormatException {

	private static final long serialVersionUID = 7146173252840739634L;

	public DataTypeException() {
	}

	public DataTypeException(String message) {
		super(message);
	}

	public DataTypeException(String message, Throwable cause) {
		super(message, cause);
	}

	public DataTypeException(Throwable cause) {
		super(cause);
	}
}
