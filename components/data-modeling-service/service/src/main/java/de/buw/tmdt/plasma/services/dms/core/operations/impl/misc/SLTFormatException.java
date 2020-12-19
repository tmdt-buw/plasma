package de.buw.tmdt.plasma.services.dms.core.operations.impl.misc;

public class SLTFormatException extends RuntimeException {
	private static final long serialVersionUID = -42585109777280895L;

	public SLTFormatException() {
	}

	public SLTFormatException(String message) {
		super(message);
	}

	public SLTFormatException(String message, Throwable cause) {
		super(message, cause);
	}

	public SLTFormatException(Throwable cause) {
		super(cause);
	}
}
