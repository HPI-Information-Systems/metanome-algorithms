package de.hpi.is.md.impl.result;

import de.hpi.is.md.result.ResultListener;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FileResultWriter<T> implements ResultListener<T>, Closeable {

	@NonNull
	private final PrintWriter out;

	public FileResultWriter(File file) throws IOException {
		this(new PrintWriter(new BufferedWriter(new FileWriter(file))));
	}

	@Override
	public void close() {
		out.close();
	}

	@Override
	public void receiveResult(T result) {
		out.println(result);
		out.flush();
	}
}
