package de.hpi.is.md.demo.input;

public interface IOReceiver {

	String getContinueKeyword();

	String getEndKeyword();

	void runOnce(IOProvider provider);
}
