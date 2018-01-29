package de.hpi.is.md.hybrid;

import de.hpi.is.md.ColumnMapping;
import de.hpi.is.md.util.Dictionary;
import java.io.Serializable;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class Preprocessed implements Serializable {

	private static final long serialVersionUID = 7398108455873873674L;
	@NonNull
	private final List<PreprocessedColumnPair> columnPairs;
	@NonNull
	private final DictionaryRecords leftRecords;
	@NonNull
	private final DictionaryRecords rightRecords;
	@NonNull
	private final List<ColumnMapping<?>> mappings;
	@NonNull
	private final List<Dictionary<?>> leftDictionaries;
	@NonNull
	private final List<Dictionary<?>> rightDictionaries;
}
