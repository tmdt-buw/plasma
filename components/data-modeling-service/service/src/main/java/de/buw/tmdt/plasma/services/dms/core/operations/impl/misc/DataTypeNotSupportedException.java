package de.buw.tmdt.plasma.services.dms.core.operations.impl.misc;

public class DataTypeNotSupportedException extends Exception {
	private static final long serialVersionUID = -923776418718241941L;

	public DataTypeNotSupportedException() {
	}

	public DataTypeNotSupportedException(String s) {
		super(s);
	}

	public DataTypeNotSupportedException(String s, Throwable throwable) {
		super(s, throwable);
	}

	public DataTypeNotSupportedException(Throwable throwable) {
		super(throwable);
	}

	protected DataTypeNotSupportedException(String s, Throwable throwable, boolean b, boolean b1) {
		super(s, throwable, b, b1);
	}
}