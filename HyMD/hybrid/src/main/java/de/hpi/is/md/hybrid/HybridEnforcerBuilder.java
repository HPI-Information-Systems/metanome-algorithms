package de.hpi.is.md.hybrid;

import de.hpi.is.md.hybrid.impl.preprocessed.PreprocessorImpl;
import de.hpi.is.md.relational.Relation;
import de.hpi.is.md.util.DiskCache;
import de.hpi.is.md.util.enforce.EnforcerBuilder;
import de.hpi.is.md.util.enforce.HybridMDEnforcer;
import de.hpi.is.md.util.enforce.MDEnforcer;
import java.io.File;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.NonNull;

@Builder
public class HybridEnforcerBuilder implements EnforcerBuilder {

	@NonNull
	private final MDMapping mappings;
	@SuppressWarnings("FieldMayBeFinal")
	@Default
	private boolean store = true;
	@SuppressWarnings("FieldMayBeFinal")
	@Default
	@NonNull
	private File cacheDirectory = new File("preprocessed/");

	@Override
	public MDEnforcer create(Relation r, Relation s) {
		Preprocessed preprocessed = preprocess(r, s);
		return HybridMDEnforcer.create(preprocessed);
	}

	private Preprocessor createPreprocessor(Relation r, Relation s) {
		PreprocessingConfiguration preprocessingConfiguration = mappings
			.getPreprocessingConfiguration();
		return PreprocessorImpl.builder()
			.mappings(preprocessingConfiguration)
			.left(r)
			.right(s)
			.build();
	}

	private Preprocessed preprocess(Relation r, Relation s) {
		Preprocessor preprocessor = createPreprocessor(r, s);
		DiskCache<Preprocessed> cache = new DiskCache<>(preprocessor, cacheDirectory);
		return cache.get(store);
	}

}
