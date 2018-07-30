package de.hpi.is.md.util;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAnd;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import de.hpi.is.md.util.FileUtils.WithFile;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class FileUtilsTest {

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void testWriteLines() throws IOException {
		WithFile withFile = FileUtils.with(folder.newFile());
		withFile.writeLines(Arrays.asList("foo", "bar"));
		Optional<Collection<String>> lines = withFile.readLines();
		assertThat(lines, isPresentAnd(hasSize(2)));
		assertThat(lines, isPresentAnd(hasItem("foo")));
		assertThat(lines, isPresentAnd(hasItem("bar")));
	}

}