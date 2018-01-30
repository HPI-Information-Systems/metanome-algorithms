package de.hpi.is.md.util;

import com.google.common.io.Files;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public final class StoredObjectService {

	@NonNull
	private final File file;

	public boolean exists() {
		return file.exists();
	}

	public Object read() throws IOException, ClassNotFoundException, ClassCastException {
		try (InputStream fin = new FileInputStream(file);
			ObjectInput in = new ObjectInputStream(fin)) {
			Object object = in.readObject();
			log.info("Read object from {}", file);
			return object;
		}
	}

	public void store(Object object) throws IOException {
		Files.createParentDirs(file);
		try (ObjectOutput out = new ObjectOutputStream(new FileOutputStream(file))) {
			out.writeObject(object);
			log.info("Stored object in {}", file);
		} catch (IOException e) {
			file.delete();
			throw e;
		}
	}
}
