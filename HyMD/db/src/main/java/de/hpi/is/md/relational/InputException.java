package de.hpi.is.md.relational;

public abstract class InputException extends RuntimeException {

	private static final long serialVersionUID = 1451389975923156317L;

	InputException(Throwable cause) {
		super(cause);
	}

	InputException(String message, Throwable cause) {
		super(message, cause);
	}

}
