package de.hpi.is.md.hybrid.impl;

import de.hpi.is.md.hybrid.DictionaryRecords;
import de.hpi.is.md.hybrid.PositionListIndex;
import de.hpi.is.md.hybrid.PreprocessedColumnPair;
import de.hpi.is.md.util.CollectionUtils;
import java.util.Collection;
import java.util.List;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;

public interface RecordGrouper {

	static RecordGrouper create(List<PreprocessedColumnPair> columnPairs,
		DictionaryRecords leftRecords) {
		//TODO for sake of completeness, add empty record grouper
		PositionListIndex first = CollectionUtils.head(columnPairs)
			.map(PreprocessedColumnPair::getLeftPli)
			.orElseThrow(IllegalArgumentException::new);
		Collection<PreprocessedColumnPair> remaining = CollectionUtils.tail(columnPairs);
		return new RecordGrouperImpl(leftRecords, first, remaining);
	}

	Seq<Tuple2<Selector, Collection<int[]>>> buildSelectors();
}
