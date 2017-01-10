package de.metanome.algorithms.mvddet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class MvdFinder {
	
	public List<MvD> findMvDs(List<List<String>> records, MvDAlgorithmConfig algorithmConfig){
		long fullTime = System.currentTimeMillis();
		
		Relation rel = new Relation(records);
		long loadRelationTime = System.currentTimeMillis() - fullTime;
		
		return findMvDs(rel, algorithmConfig, fullTime, loadRelationTime);
	}
	
	
	public static List<MvD> findMvDs(Relation rel, MvDAlgorithmConfig algorithmConfig, long fullTime, long loadRelationTime){

		
		if (algorithmConfig.isRemoveDuplicates()) {
			System.out.print("Removing duplicates...");
			rel.removeDuplicates();
			System.out.println(" Duplicates removed.");
		}
		
		
		if (algorithmConfig.isConvertToIntTuples() && algorithmConfig.isUsePLIs() && algorithmConfig.isMarkUniqueValues()) {
			System.out.print("Converting to Int tuples, generating intial PLIs, marking unique values...");
			rel.ConvertToIntTuplesUniquePLI();
			System.out.println(" Operations done.");
		}
		
		else if (algorithmConfig.isConvertToIntTuples() && algorithmConfig.isUsePLIs() && !algorithmConfig.isMarkUniqueValues()) {
			System.out.print("Converting to Int tuples, generating intial PLIs...");
			rel.ConvertToIntTuplesPLI();
			System.out.println(" Operations done.");
		}
		
		else if (algorithmConfig.isConvertToIntTuples() && !algorithmConfig.isUsePLIs() && algorithmConfig.isMarkUniqueValues()) {
			System.out.print("Converting to Int tuples, marking unique values...");
			rel.ConvertToIntTuplesUnique();
			System.out.println(" Operations done.");
		}
		
		else if (algorithmConfig.isConvertToIntTuples() && !algorithmConfig.isUsePLIs() && !algorithmConfig.isMarkUniqueValues()) {
			System.out.print("Converting to Int tuples...");
			rel.ConvertToIntTuples();
			System.out.println(" Operations done.");
		}
		
		else if (!algorithmConfig.isConvertToIntTuples() && algorithmConfig.isUsePLIs() && algorithmConfig.isMarkUniqueValues()) {
			System.out.print("Generating intial PLIs, marking unique values...");
			rel.ConvertToUniquePLI();
			System.out.println(" Operations done.");
		}
		
		else if (!algorithmConfig.isConvertToIntTuples() && algorithmConfig.isUsePLIs() && !algorithmConfig.isMarkUniqueValues()) {
			System.out.print("Generating intial PLIs...");
			rel.ConvertToPLI();
			System.out.println(" Operations done.");
		}
		
		else if (!algorithmConfig.isConvertToIntTuples() && !algorithmConfig.isUsePLIs() && algorithmConfig.isMarkUniqueValues()) {
			System.out.print("Marking unique values...");
			rel.ConvertToUnique();
			System.out.println(" Operations done.");
		}
		
		else {
			System.out.println("No further pre-operations required.");
		}
	
		System.out.print("Starting pruning algorithm " + algorithmConfig.getPruningType().name() + "...");
		
		ColumnBasedMvdFinder finder = new ColumnBasedMvdFinder();
		finder.setAlgorithmConfig(algorithmConfig);
		HashSet<MvD> mvds = new HashSet<MvD>();
		
		if (algorithmConfig.getPruningType() == MvDAlgorithmConfig.PruningType.NO_PRUNING) {
			mvds = finder.findMvdsNoPruning(rel);
		}
		
		else if (algorithmConfig.getPruningType() == MvDAlgorithmConfig.PruningType.RELEVANT_ONLY) {
			mvds = finder.findMvdsRelevantOnlyPruning(rel);
		}
		
		else if (algorithmConfig.getPruningType() == MvDAlgorithmConfig.PruningType.RELEVANT_NON_COMPLEMENT) {
			mvds = finder.findMvdsRelevantNonComplementPruning(rel);
		}
		
		else if (algorithmConfig.getPruningType() == MvDAlgorithmConfig.PruningType.BOTTOM_UP) {
			mvds = finder.findMvdsBottomUpPruning(rel);
		}
		
		else if (algorithmConfig.getPruningType() == MvDAlgorithmConfig.PruningType.TOP_DOWN) {
			mvds = finder.findMvdsTopDownPruning(rel);
		}
		
		else if (algorithmConfig.getPruningType() == MvDAlgorithmConfig.PruningType.LHS_FIRST) {
			mvds = finder.findMvdsLhsFirstPruning(rel);
		}

		else return null;
		
		System.out.println(" Pruning done.");
		
		System.out.print("Minimizing " + mvds.size() + " found MVDs...");
		
		List<MvD> minimalMvds = minimizeMvds(mvds);

		System.out.println(" Minimizing done.");
		System.out.println("Found " + minimalMvds.size() + " minimal MvDs.");
		
		fullTime = System.currentTimeMillis() - fullTime;
		System.out.println("\nAll operations finished! All done in " + fullTime + "ms!\n\n");
		
		return minimalMvds;
	}
	
	static List<MvD> minimizeMvds(HashSet<MvD> mvds){
		HashSet<MvD> nonTrivialMvds = new HashSet<MvD>();
		List<MvD> minimalMvds = new ArrayList<MvD>();
		
		for (MvD mvd : mvds){
			List<Integer> overlap = new ArrayList<Integer>();
			overlap.addAll(mvd.getLeftHandSide());
			overlap.retainAll(mvd.getRightHandSide());
			if (!mvd.getRightHandSide().isEmpty() && overlap.isEmpty() && !mvd.getRemainingAttributes().isEmpty())
				nonTrivialMvds.add(mvd);
		}
		
		for (MvD mvd : nonTrivialMvds){
			if (mvd.isMinimal(nonTrivialMvds))
				minimalMvds.add(mvd);
		}
		return minimalMvds;
	}
	
}







