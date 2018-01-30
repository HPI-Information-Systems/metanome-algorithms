package de.hpi.is.md;

import de.hpi.is.md.util.StreamUtils;
import java.util.Collection;
import lombok.Data;
import lombok.NonNull;

@Data
public class MatchingDependency {

	@NonNull
	private final Collection<ColumnMatchWithThreshold<?>> lhs;
	@NonNull
	private final ColumnMatchWithThreshold<?> rhs;

	@Override
	public String toString() {
		return "[" + StreamUtils.seq(lhs).toString(",") + "]->" + rhs;
	}

	@Data
	public static class ColumnMatchWithThreshold<T> {

		@NonNull
		private final ColumnMapping<T> match;
		@NonNull
		private final double threshold;

		@Override
		public String toString() {
			return match.toString() + "@" + threshold;
		}

	}
}
