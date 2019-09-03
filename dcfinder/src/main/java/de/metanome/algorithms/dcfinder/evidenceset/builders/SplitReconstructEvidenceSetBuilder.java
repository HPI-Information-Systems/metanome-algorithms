package de.metanome.algorithms.dcfinder.evidenceset.builders;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.metanome.algorithms.dcfinder.evidenceset.IEvidenceSet;
import de.metanome.algorithms.dcfinder.evidenceset.TroveEvidenceSet;
import de.metanome.algorithms.dcfinder.input.Input;
import de.metanome.algorithms.dcfinder.predicates.PredicateBuilder;
import de.metanome.algorithms.dcfinder.predicates.sets.PredicateSet;
import edu.stanford.nlp.util.Interval;

public class SplitReconstructEvidenceSetBuilder {

	private Input input;
	private PredicateBuilder predicateBuilder;
	private long fullTuplePairsRange;
	private long chunkLength;
	private int bufferLength;

	private List<Interval<Long>> chunckIntervals;

	IEvidenceSet fullEvidenceSet;

	public SplitReconstructEvidenceSetBuilder(Input input, PredicateBuilder predicates, long chunkLength,
			int bufferLength) {
		this.input = input;
		this.predicateBuilder = predicates;
		this.chunkLength = chunkLength;
		this.bufferLength = bufferLength;

		fullTuplePairsRange = (long) input.getLineCount() * (long) input.getLineCount();

		chunckIntervals = new ArrayList<>();
		for (long i = 0; i < fullTuplePairsRange; i += chunkLength) {
			chunckIntervals.add(Interval.toInterval(i, i + chunkLength, Interval.INTERVAL_OPEN_END));
		}
		chunckIntervals.get(chunckIntervals.size() - 1).setSecond(fullTuplePairsRange);

		log.info("First level chunks: " + chunckIntervals.size());
		BufferedEvidenceSetBuilder.configure(bufferLength, input.getLineCount(), predicateBuilder);
	}

	public void buildEvidenceSet() {

		int processors = Runtime.getRuntime().availableProcessors();
		log.info("Available processors: " + processors);

		List<List<Interval<Long>>> listOfchunckIntervals = new ArrayList<>(); 
																				
		int numPartialEvidenceSets = processors * 4; // merge partial eviset from time to time based on this number

		for (int i = 0; i < chunckIntervals.size(); i += numPartialEvidenceSets) {

			if (i + numPartialEvidenceSets >= chunckIntervals.size()) {
				listOfchunckIntervals.add(chunckIntervals.subList(i, chunckIntervals.size()));
				break;
			}
			listOfchunckIntervals.add(chunckIntervals.subList(i, i + numPartialEvidenceSets));
		}

		fullEvidenceSet = new TroveEvidenceSet();
		
		log.info("Building the Evidence Set...");

		for (List<Interval<Long>> chunkForThreads : listOfchunckIntervals) {

			Queue<IEvidenceSet> partialEvidenceSets = new ConcurrentLinkedDeque<IEvidenceSet>();

			chunkForThreads.parallelStream().forEach(interval -> {

				BufferedEvidenceSetBuilder partialEviSetBuilder = new BufferedEvidenceSetBuilder(interval,
						(interval.second - interval.first));
				IEvidenceSet partialEvidenceSet = partialEviSetBuilder.buildPartialEvidenceSet();

				partialEvidenceSets.add(partialEvidenceSet);
			});

			for (IEvidenceSet evis : partialEvidenceSets) {
				for (PredicateSet ps : evis) {
					fullEvidenceSet.add(ps, evis.getCount(ps));
				}
			}

		}

		fullEvidenceSet.getSetOfPredicateSets().remove(BufferedEvidenceSetBuilder.getCardinalityMask()); // remove reflexive tps
	}

	public IEvidenceSet getFullEvidenceSet() {
		return fullEvidenceSet;
	}

	private static Logger log = LoggerFactory.getLogger(SplitReconstructEvidenceSetBuilder.class);

}
