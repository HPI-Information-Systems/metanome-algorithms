package de.hpi.is.md.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import java.util.Arrays;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.junit.Test;

public class HasherTest {

	private static final HashFunction HASH_FUNCTION = Hashing.goodFastHash(10);

	@Test
	public void testPut() {
		assertThat(Hasher.of(HASH_FUNCTION)
				.put(new TestHashable(0))
				.put(new TestHashable(1))
			.hash()).isEqualTo(HASH_FUNCTION.newHasher().putInt(0).putInt(1).hash());
	}

	@Test
	public void testPutAll() {
		assertThat(Hasher.of(HASH_FUNCTION)
				.putAll(Arrays.asList(new TestHashable(0), new TestHashable(1)))
				.putAll(Collections.singletonList(new TestHashable(2)))
			.hash()).isEqualTo(HASH_FUNCTION.newHasher().putInt(0).putInt(1).putInt(2).hash());
	}

	@Test
	public void testPutAllNull() {
		assertThat(Hasher.of(HASH_FUNCTION)
				.putAll(null)
				.putAll(Collections.singletonList(new TestHashable(2)))
			.hash()).isEqualTo(HASH_FUNCTION.newHasher().putInt(2).hash());
	}

	@Test
	public void testPutAllOneNull() {
		assertThat(Hasher.of(HASH_FUNCTION)
				.putAll(Arrays.asList(null, new TestHashable(1)))
				.putAll(Collections.singletonList(new TestHashable(2)))
			.hash()).isEqualTo(HASH_FUNCTION.newHasher().putInt(1).putInt(2).hash());
	}

	@Test
	public void testPutBoolean() {
		assertThat(Hasher.of(HASH_FUNCTION).putBoolean(true).putBoolean(false).hash())
			.isEqualTo(HASH_FUNCTION.newHasher().putBoolean(true).putBoolean(false).hash());
	}

	@Test
	public void testPutByte() {
		assertThat(Hasher.of(HASH_FUNCTION).putByte((byte) 0).putByte((byte) 1).hash())
			.isEqualTo(HASH_FUNCTION.newHasher().putByte((byte) 0).putByte((byte) 1).hash());
	}

	@Test
	public void testPutBytes() {
		assertThat(Hasher.of(HASH_FUNCTION)
				.putBytes(new byte[]{0, 1})
				.putBytes(new byte[]{2})
			.hash())
			.isEqualTo(HASH_FUNCTION.newHasher().putBytes(new byte[]{0, 1}).putBytes(new byte[]{2})
				.hash());
	}

	@Test
	public void testPutChar() {
		assertThat(Hasher.of(HASH_FUNCTION).putChar('a').putChar('b').hash())
			.isEqualTo(HASH_FUNCTION.newHasher().putChar('a').putChar('b').hash());
	}

	@Test
	public void testPutClass() {
		assertThat(Hasher.of(HASH_FUNCTION)
				.putClass(HasherTest.class)
				.putClass(Hasher.class)
			.hash())
			.isEqualTo(HASH_FUNCTION.newHasher().putUnencodedChars(HasherTest.class.getName())
				.putUnencodedChars(Hasher.class.getName()).hash());
	}

	@Test
	public void testPutDouble() {
		assertThat(Hasher.of(HASH_FUNCTION).putDouble(0.0).putDouble(1.0).hash())
			.isEqualTo(HASH_FUNCTION.newHasher().putDouble(0.0).putDouble(1.0).hash());
	}

	@Test
	public void testPutFloat() {
		assertThat(Hasher.of(HASH_FUNCTION).putFloat(0.0f).putFloat(1.0f).hash())
			.isEqualTo(HASH_FUNCTION.newHasher().putFloat(0.0f).putFloat(1.0f).hash());
	}

	@Test
	public void testPutInt() {
		assertThat(Hasher.of(HASH_FUNCTION).putInt(0).putInt(1).hash())
			.isEqualTo(HASH_FUNCTION.newHasher().putInt(0).putInt(1).hash());
	}

	@Test
	public void testPutLong() {
		assertThat(Hasher.of(HASH_FUNCTION).putLong(0L).putLong(1L).hash())
			.isEqualTo(HASH_FUNCTION.newHasher().putLong(0L).putLong(1L).hash());
	}

	@Test
	public void testPutNull() {
		assertThat(Hasher.of(HASH_FUNCTION)
				.put(null)
				.put(new TestHashable(1))
			.hash()).isEqualTo(HASH_FUNCTION.newHasher().putInt(1).hash());
	}

	@Test
	public void testPutNullBytes() {
		assertThat(Hasher.of(HASH_FUNCTION)
				.putBytes(null)
				.putBytes(new byte[]{2})
			.hash()).isEqualTo(HASH_FUNCTION.newHasher().putBytes(new byte[]{2})
			.hash());
	}

	@Test
	public void testPutNullClass() {
		assertThat(Hasher.of(HASH_FUNCTION)
				.putClass(null)
				.putClass(Hasher.class)
			.hash())
			.isEqualTo(HASH_FUNCTION.newHasher().putUnencodedChars(Hasher.class.getName()).hash());
	}

	@Test
	public void testPutNullUnencodedChars() {
		assertThat(Hasher.of(HASH_FUNCTION)
				.putUnencodedChars(null)
				.putUnencodedChars("bar")
			.hash()).isEqualTo(HASH_FUNCTION.newHasher().putUnencodedChars("bar").hash());
	}

	@Test
	public void testPutShort() {
		assertThat(Hasher.of(HASH_FUNCTION).putShort((short) 0).putShort((short) 1).hash())
			.isEqualTo(HASH_FUNCTION.newHasher().putShort((short) 0).putShort((short) 1).hash());
	}

	@Test
	public void testPutUnencodedChars() {
		assertThat(Hasher.of(HASH_FUNCTION)
				.putUnencodedChars("foo")
				.putUnencodedChars("bar")
			.hash()).isEqualTo(
			HASH_FUNCTION.newHasher().putUnencodedChars("foo").putUnencodedChars("bar").hash());
	}

	@RequiredArgsConstructor
	private static class TestHashable implements Hashable {

		private final int value;

		@Override
		public void hash(Hasher hasher) {
			hasher.putInt(value);
		}
	}

}