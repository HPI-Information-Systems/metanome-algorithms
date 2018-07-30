package de.hpi.is.md.demo.input;

import java.util.Optional;

public interface IOProvider {

	Optional<String> readLine();

	default Optional<String> readLine(String s) {
		writeLine(s);
		return readLine();
	}

	void writeLine(String s);
}
