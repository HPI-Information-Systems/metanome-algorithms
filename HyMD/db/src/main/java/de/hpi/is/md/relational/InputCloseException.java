package de.hpi.is.md.relational;

public class InputCloseException extends InputException {

	private static final long serialVersionUID = -5364416885339689164L;

	public InputCloseException(String message, Throwable cause) {
		super(message, cause);
	}

	public InputCloseException(Throwable cause) {
		super(cause);
	}
}
