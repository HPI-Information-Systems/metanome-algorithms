package de.hpi.is.md.hybrid.impl;

import com.codahale.metrics.annotation.Timed;
import de.hpi.is.md.hybrid.DictionaryRecords;
import de.hpi.is.md.util.CollectionUtils;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntLists;
import java.util.ArrayList;
import java.util.Collection;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

//@Metrics
@RequiredArgsConstructor
public class LhsSelector {

	@NonNull
	private final Collection<ColumnPairWithThreshold> lhs;
	@NonNull
	private final DictionaryRecords rightRecords;

	@Timed
	public IntCollection findLhsMatches(Selector selector) {
		Collection<IntCollection> clusters = emptyList();
		int i = 0;
		for (ColumnPairWithThreshold pair : lhs) {
			int value = selector.get(i);
			IntCollection cluster = pair.getMatching(value);
			if (cluster.isEmpty()) {
				return IntLists.EMPTY_LIST;
			}
			clusters.add(cluster);
			i++;
		}
		return CollectionUtils.intersection(clusters)
			.orElseGet(rightRecords::getAll);
	}

	private Collection<IntCollection> emptyList() {
		int size = lhs.size();
		return new ArrayList<>(size);
	}
}
