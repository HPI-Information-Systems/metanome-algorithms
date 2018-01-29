package de.hpi.is.md.impl.result;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class FileResultWriterTest {

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void test() throws IOException {
		File file = folder.newFile();
		try (FileResultWriter<String> resultWriter = new FileResultWriter<>(file)) {
			resultWriter.receiveResult("foo");
			resultWriter.receiveResult("bar");
		}
		try (BufferedReader in = new BufferedReader(new FileReader(file))) {
			assertThat(in.readLine()).isEqualTo("foo");
			assertThat(in.readLine()).isEqualTo("bar");
		}
	}

	@Test
	public void testWriteNull() throws IOException {
		File file = folder.newFile();
		try (FileResultWriter<String> resultWriter = new FileResultWriter<>(file)) {
			resultWriter.receiveResult(null);
		}
		try (BufferedReader in = new BufferedReader(new FileReader(file))) {
			assertThat(in.readLine()).isEqualTo(Objects.toString(null));
		}
	}

}