package de.hpi.is.md.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import java.io.File;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class DiskCacheTest {

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();
	@Mock
	private CacheableSupplier<Integer> supplier;

	@Test
	public void testNoStore() {
		when(supplier.get()).thenReturn(1);
		File f = folder.getRoot();
		DiskCache<Integer> cache = new DiskCache<>(supplier, f);
		assertThat(cache.get(false)).isEqualTo(1);
		verify(supplier).get();
		assertThat(f.list()).isEmpty();
	}

	@Test
	public void testStore() {
		HashCode hash = Hashing.sha256().newHasher()
			.putBoolean(true)
			.hash();
		when(supplier.get()).thenReturn(1);
		when(supplier.hash()).thenReturn(hash);
		File f = folder.getRoot();
		DiskCache<Integer> cache = new DiskCache<>(supplier, f);
		assertThat(cache.get(true)).isEqualTo(1);
		verify(supplier).get();
		assertThat(f.list()).hasSize(1);
		assertThat(cache.get(true)).isEqualTo(1);
		verify(supplier).get();
	}

	@Test
	public void testStoreAndDelete() {
		HashCode hash = Hashing.sha256().newHasher()
			.putBoolean(true)
			.hash();
		when(supplier.get()).thenReturn(1);
		when(supplier.hash()).thenReturn(hash);
		File f = folder.getRoot();
		DiskCache<Integer> cache = new DiskCache<>(supplier, f);
		assertThat(cache.get(true)).isEqualTo(1);
		verify(supplier).get();
		folder.delete();
		assertThat(cache.get(true)).isEqualTo(1);
		verify(supplier, times(2)).get();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testStoreAndGetDifferent() {
		CacheableSupplier<Integer> supplier2 = Mockito.mock(CacheableSupplier.class);
		HashCode hash1 = Hashing.sha256().newHasher()
			.putBoolean(true)
			.hash();
		HashCode hash2 = Hashing.sha256().newHasher()
			.putBoolean(false)
			.hash();
		when(supplier.hash()).thenReturn(hash1);
		when(supplier2.hash()).thenReturn(hash2);
		when(supplier.get()).thenReturn(1);
		when(supplier2.get()).thenReturn(2);
		File f = folder.getRoot();
		DiskCache<Integer> cache = new DiskCache<>(supplier, f);
		assertThat(cache.get(true)).isEqualTo(1);
		DiskCache<Integer> cache2 = new DiskCache<>(supplier2, f);
		assertThat(cache2.get(true)).isEqualTo(2);
	}

}