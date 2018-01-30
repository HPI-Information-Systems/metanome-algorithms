package de.hpi.is.md.hybrid.impl.sim;

import com.bakdata.util.jackson.CPSBase;
import com.bakdata.util.jackson.CPSTypeIdResolver;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import de.hpi.is.md.hybrid.impl.sim.PreprocessedSimilarity.To;
import de.hpi.is.md.util.Hashable;
import de.hpi.is.md.util.Int2Int2DoubleTable;
import java.util.Collection;

public interface SimilarityTableBuilder {

	void add(int left, Collection<To> similarities);

	Int2Int2DoubleTable build();

	@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "type")
	@JsonTypeIdResolver(CPSTypeIdResolver.class)
	@CPSBase
	interface Factory extends Hashable {

		SimilarityTableBuilder create(int height, int width);
	}
}
