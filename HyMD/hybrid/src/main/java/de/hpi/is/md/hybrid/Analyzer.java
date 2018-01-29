package de.hpi.is.md.hybrid;

import de.hpi.is.md.hybrid.impl.level.AnalyzeTask;
import de.hpi.is.md.hybrid.impl.level.Statistics;

public interface Analyzer {

	Statistics analyze(Iterable<AnalyzeTask> results);
}
