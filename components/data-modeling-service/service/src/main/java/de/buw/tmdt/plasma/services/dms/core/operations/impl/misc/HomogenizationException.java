package de.buw.tmdt.plasma.services.dms.core.operations.impl.misc;

public class HomogenizationException extends Exception {
	private static final long serialVersionUID = 4254101301572740179L;

	public HomogenizationException() {
	}

	public HomogenizationException(String s) {
		super(s);
	}

	public HomogenizationException(String s, Throwable throwable) {
		super(s, throwable);
	}

	public HomogenizationException(Throwable throwable) {
		super(throwable);
	}

	protected HomogenizationException(String s, Throwable throwable, boolean b, boolean b1) {
		super(s, throwable, b, b1);
	}
}