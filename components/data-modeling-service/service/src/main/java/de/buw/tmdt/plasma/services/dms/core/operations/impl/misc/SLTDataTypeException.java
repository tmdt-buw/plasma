package de.buw.tmdt.plasma.services.dms.core.operations.impl.misc;

public class SLTDataTypeException extends SLTFormatException {

	private static final long serialVersionUID = 7146173252840739634L;

	public SLTDataTypeException() {
	}

	public SLTDataTypeException(String message) {
		super(message);
	}

	public SLTDataTypeException(String message, Throwable cause) {
		super(message, cause);
	}

	public SLTDataTypeException(Throwable cause) {
		super(cause);
	}
}
