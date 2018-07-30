package de.hpi.is.md.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.junit.Test;

public class HashableTest {

	@Test
	public void test() {
		HashFunction function = Hashing.goodFastHash(10);
		assertThat(Hasher.of(function).put(new TestHashable()).hash())
			.isEqualTo(function.newHasher().putUnencodedChars(TestHashable.class.getName()).hash());
	}

	private static class TestHashable implements Hashable {

	}

}