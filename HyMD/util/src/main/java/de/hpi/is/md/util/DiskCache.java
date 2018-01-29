package de.hpi.is.md.util;

import com.google.common.hash.HashCode;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class DiskCache<T extends Serializable> {

	@NonNull
	private final CacheableSupplier<T> supplier;
	@NonNull
	private final File file;

	public T get(boolean store) {
		final T obj;
		if (store) {
			obj = readOrCreate();
		} else {
			obj = create();
		}
		return obj;
	}

	private T create() {
		return supplier.get();
	}

	private T readOrCreate() {
		HashCode hash = supplier.hash();
		return with(hash).readOrCreate();
	}

	private WithHash with(HashCode hash) {
		return new WithHash(hash);
	}

	@RequiredArgsConstructor
	private class WithHash {

		@NonNull
		private final HashCode hash;

		private T createAndStore() {
			T obj;
			obj = create();
			store(obj);
			return obj;
		}

		private boolean exists() {
			StoredObjectService service = getService();
			return service.exists();
		}

		private File getFile() {
			String fileName = hash + ".dat";
			return new File(file, fileName);
		}

		private StoredObjectService getService() {
			File file = getFile();
			return new StoredObjectService(file);
		}

		@SuppressWarnings("unchecked")
		private T read() {
			try {
				StoredObjectService service = getService();
				return (T) service.read();
			} catch (IOException | ClassNotFoundException | ClassCastException e) {
				log.warn("Error reading object. Falling back", e);
				return create();
			}
		}

		private T readOrCreate() {
			final T obj;
			if (exists()) {
				obj = read();
			} else {
				obj = createAndStore();
			}
			return obj;
		}

		private void store(T obj) {
			try {
				StoredObjectService service = getService();
				service.store(obj);
			} catch (IOException e) {
				log.warn("Error storing object", e);
			}
		}
	}

}
