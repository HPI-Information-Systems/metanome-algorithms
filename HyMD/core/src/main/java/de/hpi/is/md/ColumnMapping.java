package de.hpi.is.md;

import de.hpi.is.md.relational.ColumnPair;
import de.hpi.is.md.sim.SimilarityMeasure;
import de.hpi.is.md.util.Hashable;
import de.hpi.is.md.util.Hasher;
import java.io.Serializable;
import lombok.Data;
import lombok.NonNull;

@Data
public class ColumnMapping<T> implements Serializable, Hashable {

	private static final long serialVersionUID = -158891076140735670L;
	@NonNull
	private final ColumnPair<T> columns;
	@NonNull
	private final SimilarityMeasure<T> similarityMeasure;

	@Override
	public void hash(Hasher hasher) {
		hasher
			.put(columns)
			.put(similarityMeasure);
	}

	@Override
	public String toString() {
		return columns.toString() + "(" + similarityMeasure.toString() + ")";
	}
}
