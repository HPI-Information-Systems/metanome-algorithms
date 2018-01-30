package de.hpi.is.md.hybrid.impl.preprocessed;

import de.hpi.is.md.hybrid.PositionListIndex;
import de.hpi.is.md.util.Dictionary;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
class CompressedColumn<T> {

	@NonNull
	private final Dictionary<T> dictionary;
	@NonNull
	private final PositionListIndex pli;
}
