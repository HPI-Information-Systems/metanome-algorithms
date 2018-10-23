package de.hpi.is.md.demo.input;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Optional;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
public class InputLooper implements IOProvider {

	private final BufferedReader in;
	private final PrintStream out;
	private final IOReceiver receiver;

	public static void loop(IOReceiver receiver) {
		try (BufferedReader in = createInput()) {
			PrintStream out = createOutput();
			InputLooper looper = InputLooper.builder()
				.in(in)
				.out(out)
				.receiver(receiver)
				.build();
			looper.loop();
		} catch (IOException e) {
			log.error("Error closing input", e);
		}
	}

	private static BufferedReader createInput() {
		return new BufferedReader(new InputStreamReader(System.in));
	}

	private static PrintStream createOutput() {
		return System.out;
	}

	@Override
	public Optional<String> readLine() {
		try {
			String line = in.readLine();
			return Optional.of(line);
		} catch (IOException e) {
			log.warn("Error reading line", e);
			return Optional.empty();
		}
	}

	@Override
	public void writeLine(String s) {
		out.println(s);
	}

	private boolean continueInput() {
		String cont = receiver.getContinueKeyword();
		String end = receiver.getEndKeyword();
		writeLine("Continue? (" + cont + "/" + end + ")");
		return readLine().map(this::continueInput)
			.orElse(Boolean.FALSE).booleanValue();
	}

	private boolean continueInput(String answer) {
		return !isEndKeyword(answer) && (isContinueKeyword(answer) || continueInput());
	}

	private boolean isContinueKeyword(String s) {
		return receiver.getContinueKeyword().equals(s);
	}

	private boolean isEndKeyword(String s) {
		return receiver.getEndKeyword().equals(s);
	}

	private void loop() {
		boolean continueInput;
		do {
			receiver.runOnce(this);
			continueInput = continueInput();
		} while (continueInput);
	}
}
