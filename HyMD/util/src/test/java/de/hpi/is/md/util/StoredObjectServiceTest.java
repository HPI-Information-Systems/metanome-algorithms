package de.hpi.is.md.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class StoredObjectServiceTest {

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void test() throws IOException, ClassNotFoundException {
		File file = folder.newFile();
		file.delete();
		StoredObjectService service = new StoredObjectService(file);
		assertThat(service.exists()).isFalse();
		service.store("foo");
		assertThat(service.exists()).isTrue();
		assertThat(service.read()).isEqualTo("foo");
	}

	@Ignore //this test does not work on ubuntu (i.e. travis-ci) but on windows
	@Test(expected = IOException.class)
	public void testFailedWrite() throws IOException {
		File file = folder.newFile();
		file.delete();
		StoredObjectService service = new StoredObjectService(file);
		try (RandomAccessFile raf = new RandomAccessFile(file, "rw");
			FileChannel channel = raf.getChannel();
			FileLock ignored = channel.lock()) {
			service.store("foo");
			fail();
		}
	}

}