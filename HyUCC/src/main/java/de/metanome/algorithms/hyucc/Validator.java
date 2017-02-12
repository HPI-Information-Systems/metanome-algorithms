package de.metanome.algorithms.hyucc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.util.OpenBitSet;

import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithms.hyucc.structures.IntegerPair;
import de.metanome.algorithms.hyucc.structures.PositionListIndex;
import de.metanome.algorithms.hyucc.structures.UCCSet;
import de.metanome.algorithms.hyucc.structures.UCCTree;
import de.metanome.algorithms.hyucc.structures.UCCTreeElement;
import de.metanome.algorithms.hyucc.structures.UCCTreeElementUCCPair;
import de.metanome.algorithms.hyucc.utils.Logger;

public class Validator {

//	private UCCTree negCover;
	private UCCSet negCover;
	private UCCTree posCover;
	private List<PositionListIndex> plis;
	private int[][] compressedRecords;
	private float efficiencyThreshold;
	private MemoryGuardian memoryGuardian;
	private ExecutorService executor;
	
	private int level = 1;

	public Validator(UCCSet negCover, UCCTree posCover, int[][] compressedRecords, List<PositionListIndex> plis, float efficiencyThreshold, boolean parallel, MemoryGuardian memoryGuardian) {
		this.negCover = negCover;
		this.posCover = posCover;
//		this.negCover = new UCCTree(plis.size(), -1);
		this.plis = plis;
		this.compressedRecords = compressedRecords;
		this.efficiencyThreshold = efficiencyThreshold;
		this.memoryGuardian = memoryGuardian;
		
		if (parallel) {
			int numThreads = Runtime.getRuntime().availableProcessors();
			this.executor = Executors.newFixedThreadPool(numThreads);
		}
	}
	
	private class ValidationResult {
		public int validations = 0;
		public int intersections = 0;
		public List<OpenBitSet> invalidUCCs = new ArrayList<>();
		public List<IntegerPair> comparisonSuggestions = new ArrayList<>();
		public void add(ValidationResult other) {
			this.validations += other.validations;
			this.intersections += other.intersections;
			this.invalidUCCs.addAll(other.invalidUCCs);
			this.comparisonSuggestions.addAll(other.comparisonSuggestions);
		}
	}
	
	private class ValidationTask implements Callable<ValidationResult> {
		private UCCTreeElementUCCPair elementUCCPair;
		public void setElementLhsPair(UCCTreeElementUCCPair elementLhsPair) {
			this.elementUCCPair = elementLhsPair;
		}
		public ValidationTask(UCCTreeElementUCCPair elementUCCPair) {
			this.elementUCCPair = elementUCCPair;
		}
		public ValidationResult call() throws Exception {
			ValidationResult result = new ValidationResult();
			
			UCCTreeElement element = this.elementUCCPair.getElement();
			OpenBitSet ucc = this.elementUCCPair.getUCC();
			
			result.validations = result.validations + 1;
			
			if (Validator.this.level == 1) {
				int uccAttr = ucc.nextSetBit(0);
				if (!Validator.this.plis.get(uccAttr).isUnique()) {
					element.setUCC(false);
					result.invalidUCCs.add(ucc);
				}
				result.intersections++;
			}
			else {
				int firstAttr = ucc.nextSetBit(0);
				
				ucc.clear(firstAttr);
				boolean isValid = Validator.this.plis.get(firstAttr).isUniqueWith(Validator.this.compressedRecords, ucc, result.comparisonSuggestions);
				ucc.set(firstAttr);
				
				result.intersections++;
				
				if (!isValid) {
					element.setUCC(false);
					result.invalidUCCs.add(ucc);
				}
			}
			return result;
		}
	}

	private ValidationResult validateSequential(List<UCCTreeElementUCCPair> currentLevel) throws AlgorithmExecutionException {
		ValidationResult validationResult = new ValidationResult();
		
		ValidationTask task = new ValidationTask(null);
		for (UCCTreeElementUCCPair elementLhsPair : currentLevel) {
			if (!elementLhsPair.getElement().isUCC())
				continue;
			
			task.setElementLhsPair(elementLhsPair);
			try {
				validationResult.add(task.call());
			}
			catch (Exception e) {
				e.printStackTrace();
				throw new AlgorithmExecutionException(e.getMessage());
			}
		}
		
		return validationResult;
	}
	
	private ValidationResult validateParallel(List<UCCTreeElementUCCPair> currentLevel) throws AlgorithmExecutionException {
		ValidationResult validationResult = new ValidationResult();
		
		List<Future<ValidationResult>> futures = new ArrayList<>();
		for (UCCTreeElementUCCPair elementLhsPair : currentLevel) {
			if (elementLhsPair.getElement().isUCC()) {
				ValidationTask task = new ValidationTask(elementLhsPair);
				futures.add(this.executor.submit(task));
			}
		}
		
		for (Future<ValidationResult> future : futures) {
			try {
				validationResult.add(future.get());
			}
			catch (ExecutionException e) {
				this.executor.shutdownNow();
				e.printStackTrace();
				throw new AlgorithmExecutionException(e.getMessage());
			}
			catch (InterruptedException e) {
				this.executor.shutdownNow();
				e.printStackTrace();
				throw new AlgorithmExecutionException(e.getMessage());
			}
		}
		
		return validationResult;
	}
	
	public List<IntegerPair> validatePositiveCover() throws AlgorithmExecutionException {
		int numAttributes = this.plis.size();
		
//		Logger.getInstance().writeln("Adding new non UCCs to negative cover tree ...");
//		for (OpenBitSet nonUCC : newNonUCCs)
//			this.negCover.addUniqueColumnCombination(nonUCC);
		
		Logger.getInstance().writeln("Validating UCCs using plis ...");
		
		List<UCCTreeElementUCCPair> currentLevel = null;
		currentLevel = this.posCover.getLevel(this.level);
		
		// Start the level-wise validation/discovery
		int previousNumInvalidUCCs = 0;
		List<IntegerPair> comparisonSuggestions = new ArrayList<>();
		while (!currentLevel.isEmpty()) {
			Logger.getInstance().write("\tLevel " + this.level + ": " + currentLevel.size() + " elements; ");
			
			// Validate current level
			Logger.getInstance().write("(V)");
			
			ValidationResult validationResult = (this.executor == null) ? this.validateSequential(currentLevel) : this.validateParallel(currentLevel);
			comparisonSuggestions.addAll(validationResult.comparisonSuggestions);
			
			// If the next level exceeds the predefined maximum ucc size, then we can stop here
			if ((this.posCover.getMaxDepth() > -1) && (this.level >= this.posCover.getMaxDepth())) {
				int numInvalidUCCs = validationResult.invalidUCCs.size();
				int numValidUCCs = validationResult.validations - numInvalidUCCs;
				Logger.getInstance().writeln("(-)(-); " + validationResult.intersections + " intersections; " + validationResult.validations + " validations; " + numInvalidUCCs + " invalid; " + "-" + " new candidates; --> " + numValidUCCs + " UCCs");
				break;
			}
			
			// Add all children to the next level
			Logger.getInstance().write("(C)");
			
			List<UCCTreeElementUCCPair> nextLevel = new ArrayList<>();
			for (UCCTreeElementUCCPair elementUCCPair : currentLevel) {
				UCCTreeElement element = elementUCCPair.getElement();
				OpenBitSet ucc = elementUCCPair.getUCC();

				if (element.getChildren() == null)
					continue;
				
				for (int childAttr = 0; childAttr < numAttributes; childAttr++) {
					UCCTreeElement child = element.getChildren()[childAttr];
					
					if (child != null) {
						OpenBitSet childUCC = ucc.clone();
						childUCC.set(childAttr);
						nextLevel.add(new UCCTreeElementUCCPair(child, childUCC));
					}
				}
			}
						
			// Generate new UCCs from the invalid UCCs and add them to the next level as well
			Logger.getInstance().write("(G); ");
			
			int candidates = 0;
			for (OpenBitSet invalidUCC : validationResult.invalidUCCs) {
				for (int extensionAttr = 0; extensionAttr < numAttributes; extensionAttr++) {
					OpenBitSet childUCC = this.extendWith(invalidUCC, extensionAttr);
					if (childUCC != null) {
						UCCTreeElement child = this.posCover.addUniqueColumnCombinationGetIfNew(childUCC);
						if (child != null) {
							nextLevel.add(new UCCTreeElementUCCPair(child, childUCC));
							candidates++;
							
							this.memoryGuardian.memoryChanged(1);
							this.memoryGuardian.match(this.negCover, this.posCover, null);
						}
					}
				}
				
				if ((this.posCover.getMaxDepth() > -1) && (this.level >= this.posCover.getMaxDepth()))
					break;
			}
			
			currentLevel = nextLevel;
			this.level++;
			int numInvalidUCCs = validationResult.invalidUCCs.size();
			int numValidUCCs = validationResult.validations - numInvalidUCCs;
			Logger.getInstance().writeln(validationResult.intersections + " intersections; " + validationResult.validations + " validations; " + numInvalidUCCs + " invalid; " + candidates + " new candidates; --> " + numValidUCCs + " UCCs");
		
			// Decide if we continue validating the next level or if we go back into the sampling phase
			if ((numInvalidUCCs > numValidUCCs * this.efficiencyThreshold) && (previousNumInvalidUCCs < numInvalidUCCs))
				return comparisonSuggestions;
			previousNumInvalidUCCs = numInvalidUCCs;
		}
		
		if (this.executor != null) {
			this.executor.shutdown();
			try {
				this.executor.awaitTermination(365, TimeUnit.DAYS);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	private OpenBitSet extendWith(OpenBitSet ucc, int extensionAttr) {
		if (ucc.get(extensionAttr))
			return null;
		
		OpenBitSet childUCC = ucc.clone();
		childUCC.set(extensionAttr);
		
		if (this.posCover.containsUCCOrGeneralization(childUCC))
			return null;
		
//		if (this.negCover.containsUCCOrSpecialization(childUCC)) // TODO: May be needed?
//			return null;
		
		return childUCC;
	}

}
