package de.hpi.is.md.util;

import com.google.common.io.Files;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileUtils {

	private static final Charset CHARSET = Charset.defaultCharset();

	public static WithFile with(File file) {
		return new WithFile(file);
	}

	@Slf4j
	@RequiredArgsConstructor
	public static class WithFile {

		@NonNull
		private final File file;

		public Optional<Collection<String>> readLines() {
			try {
				Collection<String> lines = read();
				return Optional.of(lines);
			} catch (IOException e) {
				log.warn("Error reading file", e);
				return Optional.empty();
			}
		}

		public boolean writeLines(Iterable<String> lines) {
			return create() && write(lines);
		}

		private boolean create() {
			try {
				Files.createParentDirs(file);
			} catch (IOException e) {
				log.warn("Error creating file", e);
				return false;
			}
			return true;
		}

		private Collection<String> read() throws IOException {
			return Files.readLines(file, CHARSET);
		}

		private boolean write(Iterable<String> lines) {
			try (PrintWriter writer = writer()) {
				lines.forEach(writer::println);
			} catch (IOException e) {
				log.warn("Error writing file", e);
				return false;
			}
			return true;
		}

		private PrintWriter writer() throws FileNotFoundException {
			Writer writer = Files.newWriter(file, CHARSET);
			return new PrintWriter(writer);
		}

	}
}
