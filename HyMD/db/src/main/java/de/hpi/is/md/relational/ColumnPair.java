package de.hpi.is.md.relational;

import de.hpi.is.md.util.Hashable;
import de.hpi.is.md.util.Hasher;
import java.io.Serializable;
import lombok.Data;
import lombok.NonNull;
import org.jooq.lambda.Seq;

@Data
public class ColumnPair<T> implements Serializable, Hashable {

	private static final long serialVersionUID = 8470259831636852058L;
	@NonNull
	private final Column<T> left;
	@NonNull
	private final Column<T> right;

	public Class<T> getType() {
		return left.getType();
	}

	@Override
	public void hash(Hasher hasher) {
		hasher
			.put(left)
			.put(right);
	}

	@Override
	public String toString() {
		return "[" + Seq.of(left, right)
			.distinct()
			.toString(", ") + "]";
	}
}
