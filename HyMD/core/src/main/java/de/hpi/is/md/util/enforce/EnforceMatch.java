package de.hpi.is.md.util.enforce;

import de.hpi.is.md.util.StreamUtils;
import java.util.Arrays;
import java.util.List;
import lombok.Data;

@Data
public class EnforceMatch {

	private final Iterable<Object[]> left;
	private final Iterable<Object[]> right;

	private static String toString(Iterable<Object[]> objects) {
		List<String> strings = StreamUtils.seq(objects)
			.map(Arrays::toString)
			.toList();
		return strings.toString();
	}

	@Override
	public String toString() {
		return toString(left) + "," + toString(right);
	}

}
