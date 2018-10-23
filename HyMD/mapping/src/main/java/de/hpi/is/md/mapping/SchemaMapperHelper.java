package de.hpi.is.md.mapping;

import de.hpi.is.md.relational.Column;
import de.hpi.is.md.relational.ColumnPair;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SchemaMapperHelper {

	@SuppressWarnings("unchecked")
	public static <T> Optional<ColumnPair<T>> toPair(Column<T> left, Column<?> right) {
		if (ofSameType(left, right)) {
			Column<T> rightT = (Column<T>) right;
			ColumnPair<T> pair = new ColumnPair<>(left, rightT);
			return Optional.of(pair);
		}
		return Optional.empty();
	}

	private static boolean ofSameType(Column<?> left, Column<?> right) {
		Class<?> rightType = right.getType();
		Class<?> leftType = left.getType();
		return leftType.equals(rightType);
	}
}
