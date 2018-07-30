package de.hpi.is.md.eval;

import static de.hpi.is.md.util.FileUtils.with;

import com.google.common.hash.HashCode;
import de.hpi.is.md.util.Differ;
import de.hpi.is.md.util.Differ.DiffResult;
import java.io.File;
import java.util.Collection;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Builder
@Slf4j
class EvaluationTask {

	@NonNull
	private final HashCode hash;
	@NonNull
	private final Collection<String> result;
	@SuppressWarnings("FieldMayBeFinal")
	@Default
	@NonNull
	private File directory = new File("gold/");

	void evaluateOrCreate() {
		File file = getFile();
		if (file.exists()) {
			evaluate(file);
		} else {
			create(file);
		}
	}

	private void create(File file) {
		if (with(file).writeLines(result)) {
			log.info("Created gold standard in {}", file);
		} else {
			log.warn("Failed to create gold standard in {}", file);
		}
	}

	private void evaluate(File file) {
		log.info("Found gold standard in {}", file);
		with(file).readLines()
			.ifPresent(this::evaluate);
	}

	private void evaluate(Collection<String> goldStandard) {
		DiffResult<String> diff = Differ.diff(result, goldStandard);
		if (diff.isSame()) {
			log.info("Results matched gold standard");
		} else {
			log.error("Results do not match gold standard: "
					+ "{} only in result, {} only in gold standard, {} same",
				diff.getOnlyA().size(), diff.getOnlyB().size(), diff.getCommon().size());
		}
	}

	private File getFile() {
		String fileName = hash + ".txt";
		return new File(directory, fileName);
	}

}
