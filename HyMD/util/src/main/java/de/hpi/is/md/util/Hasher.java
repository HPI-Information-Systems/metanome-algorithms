package de.hpi.is.md.util;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Hasher {

	private final com.google.common.hash.Hasher hasher;

	public static Hasher of(HashFunction function) {
		com.google.common.hash.Hasher hasher = function.newHasher();
		return new Hasher(hasher);
	}

	public HashCode hash() {
		return hasher.hash();
	}

	public Hasher put(Hashable hashable) {
		Optional.ofNullable(hashable)
			.ifPresent(this::hash);
		return this;
	}

	public Hasher putAll(Iterable<? extends Hashable> hashables) {
		Optional.ofNullable(hashables)
			.ifPresent(this::hashAll);
		return this;
	}

	public Hasher putBoolean(boolean b) {
		hasher.putBoolean(b);
		return this;
	}

	public Hasher putByte(byte b) {
		hasher.putByte(b);
		return this;
	}

	public Hasher putBytes(byte[] bytes) {
		Optional.ofNullable(bytes)
			.ifPresent(hasher::putBytes);
		return this;
	}

	public Hasher putChar(char c) {
		hasher.putChar(c);
		return this;
	}

	public Hasher putClass(Class<?> type) {
		Optional.ofNullable(type)
			.map(Class::getName)
			.ifPresent(hasher::putUnencodedChars);
		return this;
	}

	public Hasher putDouble(double d) {
		hasher.putDouble(d);
		return this;
	}

	public Hasher putFloat(float f) {
		hasher.putFloat(f);
		return this;
	}

	public Hasher putInt(int i) {
		hasher.putInt(i);
		return this;
	}

	public Hasher putLong(long l) {
		hasher.putLong(l);
		return this;
	}

	public Hasher putShort(short s) {
		hasher.putShort(s);
		return this;
	}

	public Hasher putUnencodedChars(CharSequence charSequence) {
		Optional.ofNullable(charSequence)
			.ifPresent(hasher::putUnencodedChars);
		return this;
	}

	private void hash(Hashable hashable) {
		hashable.hash(this);
	}

	private void hashAll(Iterable<? extends Hashable> hashables) {
		hashables.forEach(this::put);
	}
}
