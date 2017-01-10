package de.metanome.algorithms.mvddet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ColumnBasedMvdFinder {
	private MvDAlgorithmConfig algorithmConfig;
	
	
	public void setAlgorithmConfig(MvDAlgorithmConfig config){
		this.algorithmConfig = config;
	}
	
	
	private List<List<Integer>> getAllSublistsOfSize(List<Integer> list, int size){
		List<List<Integer>> sublists = new ArrayList<List<Integer>>();
		if (size > 0)
			getSublists(list, size, 0, new ArrayList<Integer>(), sublists);
		else
			sublists.add(new ArrayList<Integer>());
		return sublists;
	}
	
	private static void getSublists(List<Integer> superSet, int k, int idx, List<Integer> current, List<List<Integer>> result){
	    if (current.size() == k){
	    	result.add(new ArrayList<Integer>(current));
	        return;
	    }
	    if (idx == superSet.size())
	    	return;
	    
	    Integer x = superSet.get(idx);
	    
	    current.add(x);
	    getSublists(superSet, k, idx+1, current, result);
	    current.remove(x);
	    
	    getSublists(superSet, k, idx+1, current, result);
	}
	
	
	private boolean isMvdValid(MvD mvd, Relation rel, PositionListIndex pli){
		boolean isValid = true;
		
		if (algorithmConfig.isConvertToIntTuples() && algorithmConfig.isUsePLIs() && algorithmConfig.isMarkUniqueValues()) {
			isValid = isMvdValidUseIntTuplesPLIsUniques(mvd, rel, pli);
		}
		
		else if (algorithmConfig.isConvertToIntTuples() && algorithmConfig.isUsePLIs() && !algorithmConfig.isMarkUniqueValues()) {
			isValid = isMvdValidUseIntTuplesPLIs(mvd, rel, pli);
		}
		
		else if (algorithmConfig.isConvertToIntTuples() && !algorithmConfig.isUsePLIs() && algorithmConfig.isMarkUniqueValues()) {
			isValid = isMvdValidUseIntTuplesUniques(mvd, rel);
		}
		
		else if (algorithmConfig.isConvertToIntTuples() && !algorithmConfig.isUsePLIs() && !algorithmConfig.isMarkUniqueValues()) {
			isValid = isMvdValidUseIntTuples(mvd, rel);
		}
		
		else if (!algorithmConfig.isConvertToIntTuples() && algorithmConfig.isUsePLIs() && algorithmConfig.isMarkUniqueValues()) {
			isValid = isMvdValidUsePLIsUniques(mvd, rel, pli);
		}
		
		else if (!algorithmConfig.isConvertToIntTuples() && algorithmConfig.isUsePLIs() && !algorithmConfig.isMarkUniqueValues()) {
			isValid = isMvdValidUsePLIs(mvd, rel, pli);
		}
		
		else if (!algorithmConfig.isConvertToIntTuples() && !algorithmConfig.isUsePLIs() && algorithmConfig.isMarkUniqueValues()) {
			isValid = isMvdValidUseUniques(mvd, rel);
		}
		
		else {
			isValid = isMvdValidUseNothing(mvd, rel);
		}
		
		return isValid;
	}
	
	
	
	
	
	
	
	private boolean isMvdValidUseIntTuplesPLIsUniques(MvD mvd, Relation rel, PositionListIndex pli){
		List<Integer> rhs = mvd.getRightHandSide();
		List<Integer> rem = mvd.getRemainingAttributes();
		
		for (Set<Integer> posSet : pli.getIndices()) {
		
			for (int i : posSet) {
				Tuple t1 = rel.getTupleAt(i);
				
				for (int j : posSet) {
					if (i == j)	continue;
					
					Tuple t2 = rel.getTupleAt(j);
					
					if (t1.containsUniqueValue() || t2.containsUniqueValue()){
						if ((t1.getValuesAt(rhs).contains(0) || t2.getValuesAt(rhs).contains(0)) && !t1.getValuesAt(rem).contains(0) && t1.getValuesAt(rem).equals(t2.getValuesAt(rem)) ||
								(t1.getValuesAt(rem).contains(0) || t2.getValuesAt(rem).contains(0)) && !t1.getValuesAt(rhs).contains(0) && t1.getValuesAt(rhs).equals(t2.getValuesAt(rhs)))
							continue;
						return false;
					}
					
					Tuple nw = generateNegativeWitness(mvd, t1, t2);
					if (!rel.doesTupleExist(nw))
						return false;
				}

			}
		}
		return true;
	}
	
	
	private boolean isMvdValidUseIntTuplesPLIs(MvD mvd, Relation rel, PositionListIndex pli){
		for (Set<Integer> posSet : pli.getIndices()) {
			for (int i : posSet) {
				Tuple t1 = rel.getTupleAt(i);
				for (int j : posSet) {
					if (i == j)	continue;
					
					Tuple t2 = rel.getTupleAt(j);
					Tuple nw = generateNegativeWitness(mvd, t1, t2);
					if (!rel.doesTupleExist(nw))
						return false;
					
				}
				
			}
		}
		return true;
	}
	
	
	private boolean isMvdValidUseIntTuplesUniques(MvD mvd, Relation rel){
		int tupleCount = rel.getTupleCount();
		List<Integer> lhs = mvd.getLeftHandSide();
		List<Integer> rhs = mvd.getRightHandSide();
		List<Integer> rem = mvd.getRemainingAttributes();
		
		for (int i = 0; i < tupleCount; i++){
			Tuple t1 = rel.getTupleAt(i);
			
			if (t1.getValuesAt(lhs).contains(0))
				continue;
			
			for (int j = 0; j < tupleCount; j++){
				if (i == j) continue;
				
				Tuple t2 = rel.getTupleAt(j);
				
				if (t1.getValuesAt(lhs).equals(t2.getValuesAt(lhs))){
					
					if (t1.containsUniqueValue() || t2.containsUniqueValue()){
						if ((t1.getValuesAt(rhs).contains(0) || t2.getValuesAt(rhs).contains(0)) && !t1.getValuesAt(rem).contains(0) && t1.getValuesAt(rem).equals(t2.getValuesAt(rem)) ||
								(t1.getValuesAt(rem).contains(0) || t2.getValuesAt(rem).contains(0)) && !t1.getValuesAt(rhs).contains(0) && t1.getValuesAt(rhs).equals(t2.getValuesAt(rhs)))
							continue;
						return false;
					}
					
					Tuple nw = generateNegativeWitness(mvd, t1, t2);
					if (!rel.doesTupleExist(nw))
						return false;
				}
			}
		}
		return true;
	}
	
	
	private boolean isMvdValidUseIntTuples(MvD mvd, Relation rel){
		int tupleCount = rel.getTupleCount();
		List<Integer> lhs = mvd.getLeftHandSide();
		
		for (int i = 0; i < tupleCount; i++){
			Tuple t1 = rel.getTupleAt(i);
			for (int j = 0; j < tupleCount; j++){
				if (i == j) continue;
				
				Tuple t2 = rel.getTupleAt(j);
				
				if (t1.getValuesAt(lhs).equals(t2.getValuesAt(lhs))){
					Tuple nw = generateNegativeWitness(mvd, t1, t2);
					if (!rel.doesTupleExist(nw))
						return false;
				}
			}
		}
		return true;
	}
	
	
	private boolean isMvdValidUsePLIsUniques(MvD mvd, Relation rel, PositionListIndex pli){
		List<Integer> rhs = mvd.getRightHandSide();
		List<Integer> rem = mvd.getRemainingAttributes();
		
		for (Set<Integer> posSet : pli.getIndices()) {
		
			for (int i : posSet) {
				StringTuple t1 = rel.getStringTupleAt(i);
				
				for (int j : posSet) {
					if (i == j)	continue;
					
					StringTuple t2 = rel.getStringTupleAt(j);
					
					if (t1.containsUniqueValue() || t2.containsUniqueValue()){
						if ((t1.getValuesAt(rhs).contains("_MVD_DETECTOR_UNIQUE_VALUE_") || t2.getValuesAt(rhs).contains("_MVD_DETECTOR_UNIQUE_VALUE_")) && !t1.getValuesAt(rem).contains("_MVD_DETECTOR_UNIQUE_VALUE_") && t1.getValuesAt(rem).equals(t2.getValuesAt(rem)) ||
								(t1.getValuesAt(rem).contains("_MVD_DETECTOR_UNIQUE_VALUE_") || t2.getValuesAt(rem).contains("_MVD_DETECTOR_UNIQUE_VALUE_")) && !t1.getValuesAt(rhs).contains("_MVD_DETECTOR_UNIQUE_VALUE_") && t1.getValuesAt(rhs).equals(t2.getValuesAt(rhs)))
							continue;
						return false;
					}
					
					StringTuple nw = generateNegativeWitness(mvd, t1, t2);
					if (!rel.doesTupleExist(nw))
						return false;
				}

			}
		}
		return true;
	}
	
	
	private boolean isMvdValidUsePLIs(MvD mvd, Relation rel, PositionListIndex pli){
		for (Set<Integer> posSet : pli.getIndices()) {
			for (int i : posSet) {
				StringTuple t1 = rel.getStringTupleAt(i);
				for (int j : posSet) {
					if (i == j)	continue;
					
					StringTuple t2 = rel.getStringTupleAt(j);
					StringTuple nw = generateNegativeWitness(mvd, t1, t2);
					if (!rel.doesTupleExist(nw))
						return false;
					
				}
				
			}
		}
		return true;
	}
	
	
	private boolean isMvdValidUseUniques(MvD mvd, Relation rel){
		int tupleCount = rel.getTupleCount();
		List<Integer> lhs = mvd.getLeftHandSide();
		List<Integer> rhs = mvd.getRightHandSide();
		List<Integer> rem = mvd.getRemainingAttributes();
		
		for (int i = 0; i < tupleCount; i++){
			StringTuple t1 = rel.getStringTupleAt(i);
			
			if (t1.getValuesAt(lhs).contains("_MVD_DETECTOR_UNIQUE_VALUE_"))
				continue;
			
			for (int j = 0; j < tupleCount; j++){
				if (i == j) continue;
				
				StringTuple t2 = rel.getStringTupleAt(j);
				
				if (t1.getValuesAt(lhs).equals(t2.getValuesAt(lhs))){
					
					if (t1.containsUniqueValue() || t2.containsUniqueValue()){
						if ((t1.getValuesAt(rhs).contains("_MVD_DETECTOR_UNIQUE_VALUE_") || t2.getValuesAt(rhs).contains("_MVD_DETECTOR_UNIQUE_VALUE_")) && !t1.getValuesAt(rem).contains("_MVD_DETECTOR_UNIQUE_VALUE_") && t1.getValuesAt(rem).equals(t2.getValuesAt(rem)) ||
								(t1.getValuesAt(rem).contains("_MVD_DETECTOR_UNIQUE_VALUE_") || t2.getValuesAt(rem).contains("_MVD_DETECTOR_UNIQUE_VALUE_")) && !t1.getValuesAt(rhs).contains("_MVD_DETECTOR_UNIQUE_VALUE_") && t1.getValuesAt(rhs).equals(t2.getValuesAt(rhs)))
							continue;
						return false;
					}
					
					StringTuple nw = generateNegativeWitness(mvd, t1, t2);
					if (!rel.doesTupleExist(nw))
						return false;
				}
			}
		}
		return true;
	}
	
	
	private boolean isMvdValidUseNothing(MvD mvd, Relation rel){
		int tupleCount = rel.getTupleCount();
		List<Integer> lhs = mvd.getLeftHandSide();
		
		for (int i = 0; i < tupleCount; i++){
			StringTuple t1 = rel.getStringTupleAt(i);
			for (int j = 0; j < tupleCount; j++){
				if (i == j) continue;
				
				StringTuple t2 = rel.getStringTupleAt(j);
				
				if (t1.getValuesAt(lhs).equals(t2.getValuesAt(lhs))){
					StringTuple nw = generateNegativeWitness(mvd, t1, t2);
					if (!rel.doesTupleExist(nw))
						return false;
				}
			}
		}
		return true;
	}

	
	
	
	private Tuple generateNegativeWitness(MvD mvd, Tuple tuple1, Tuple tuple2){
		
		int[] nw1content = new int[tuple1.size()];
		
		for (int pos : mvd.getLeftHandSide()){
			nw1content[pos] = tuple1.getValueAt(pos);
		}		
		for (int pos : mvd.getRightHandSide()){
			nw1content[pos] = tuple1.getValueAt(pos);
		}		
		for (int pos : mvd.getRemainingAttributes()){
			nw1content[pos] = tuple2.getValueAt(pos);
		}
		
		return new Tuple(nw1content);
	}
	
	private StringTuple generateNegativeWitness(MvD mvd, StringTuple tuple1, StringTuple tuple2){
		
		String[] nw1content = new String[tuple1.size()];
		
		for (int pos : mvd.getLeftHandSide()){
			nw1content[pos] = tuple1.getValueAt(pos);
		}		
		for (int pos : mvd.getRightHandSide()){
			nw1content[pos] = tuple1.getValueAt(pos);
		}		
		for (int pos : mvd.getRemainingAttributes()){
			nw1content[pos] = tuple2.getValueAt(pos);
		}
		
		return new StringTuple(nw1content);
	}
	

	
	public HashSet<MvD> findMvdsNoPruning(Relation rel){
		HashSet<MvD> foundMvds = new HashSet<MvD>();
		int attributeCount = rel.getAttributeCount();
		
		List<Integer> attributeList = new ArrayList<Integer>();
		
		for (int i = 0; i < attributeCount; i++){
			attributeList.add(i);
		}
		
		List<List<Integer>> allPossibleAttributeCombinations = new ArrayList<List<Integer>>();
		
		for (int i = 0; i <= attributeCount; i++)
			allPossibleAttributeCombinations.addAll(getAllSublistsOfSize(attributeList, i));
		
		for (List<Integer> lhs : allPossibleAttributeCombinations){
			
			PositionListIndex pli = null;
			if (algorithmConfig.isUsePLIs())
				pli = new PositionListIndex(rel.getPLIs(lhs));
			
			for (List<Integer> rhs : allPossibleAttributeCombinations){
				MvD mvd = new MvD(lhs, rhs, attributeCount);
				if (isMvdValid(mvd, rel, pli))
					foundMvds.add(mvd);
			}
		}
		return foundMvds;
	}
	
	
	public HashSet<MvD> findMvdsRelevantOnlyPruning(Relation rel){
		HashSet<MvD> foundMvds = new HashSet<MvD>();
		int attributeCount = rel.getAttributeCount();
		
		List<Integer> attributeList = new ArrayList<Integer>();
		
		for (int i = 0; i < attributeCount; i++){
			attributeList.add(i);
		}
		
		List<List<Integer>> allLhsCombinations = new ArrayList<List<Integer>>();
		
		for (int i = 0; i <= attributeCount - 2; i++)
			allLhsCombinations.addAll(getAllSublistsOfSize(attributeList, i));
		
		for (List<Integer> lhs : allLhsCombinations){
			
			List<Integer> rhsAttributes = new ArrayList<Integer>();
			rhsAttributes.addAll(attributeList);
			rhsAttributes.removeAll(lhs);
			List<List<Integer>> allRhsCombinations = new ArrayList<List<Integer>>();
			for (int j = 1; j <= rhsAttributes.size() - 1; j++)
				allRhsCombinations.addAll(getAllSublistsOfSize(rhsAttributes, j));
			
			PositionListIndex pli = null;
			if (algorithmConfig.isUsePLIs())
				pli = new PositionListIndex(rel.getPLIs(lhs));
			
			for (List<Integer> rhs : allRhsCombinations){
				MvD mvd = new MvD(lhs, rhs, attributeCount);
				if (isMvdValid(mvd, rel, pli))
					foundMvds.add(mvd);
			}
		}
		return foundMvds;
	}
	
	
	public HashSet<MvD> findMvdsRelevantNonComplementPruning(Relation rel){
		HashSet<MvD> foundMvds = new HashSet<MvD>();
		int attributeCount = rel.getAttributeCount();
		
		List<Integer> attributeList = new ArrayList<Integer>();
		
		for (int i = 0; i < attributeCount; i++){
			attributeList.add(i);
		}
		
		List<List<Integer>> allLhsCombinations = new ArrayList<List<Integer>>();
		
		for (int i = 0; i <= attributeCount - 2; i++)
			allLhsCombinations.addAll(getAllSublistsOfSize(attributeList, i));
		
		for (List<Integer> lhs : allLhsCombinations){
			
			List<Integer> rhsAttributes = new ArrayList<Integer>();
			rhsAttributes.addAll(attributeList);
			rhsAttributes.removeAll(lhs);
			List<List<Integer>> allRhsCombinations = new ArrayList<List<Integer>>();
			if (rhsAttributes.size() % 2 == 0) {
				rhsAttributes.remove(rhsAttributes.size()-1);
				for (int j = 1; j <= rhsAttributes.size(); j++)
					allRhsCombinations.addAll(getAllSublistsOfSize(rhsAttributes, j));
			}
			for (int j = 1; j <= (rhsAttributes.size() - 1) / 2; j++)
				allRhsCombinations.addAll(getAllSublistsOfSize(rhsAttributes, j));
			
			PositionListIndex pli = null;
			if (algorithmConfig.isUsePLIs())
				pli = new PositionListIndex(rel.getPLIs(lhs));
			
			for (List<Integer> rhs : allRhsCombinations){
				MvD mvd = new MvD(lhs, rhs, attributeCount);
				if (isMvdValid(mvd, rel, pli)){
					foundMvds.add(mvd);
					foundMvds.add(new MvD(lhs, mvd.getRemainingAttributes(), attributeCount));
				}
			}
		}
		return foundMvds;
	}
	
	
	public HashSet<MvD> findMvdsBottomUpPruning(Relation rel){
		HashSet<MvD> foundMvds = new HashSet<MvD>();
		HashSet<MvD> mvdsToCheck = new HashSet<MvD>();
		HashSet<MvD> checkedMvds = new HashSet<MvD>();
		int attributeCount = rel.getAttributeCount();
		
		List<Integer> attributeList = new ArrayList<Integer>();
		
		for (int i = 0; i < attributeCount; i++){
			attributeList.add(i);
			MvD mvd = new MvD(new ArrayList<Integer>(), i, attributeCount);
			mvdsToCheck.add(mvd);
		}
		
		
		while (!mvdsToCheck.isEmpty()){
			Iterator<MvD> iterator = mvdsToCheck.iterator();
			MvD mvd = iterator.next();
			iterator.remove();
			
			MvD complementMvd = new MvD(mvd.getLeftHandSide(), mvd.getRemainingAttributes(), attributeCount);
			checkedMvds.add(mvd);
			checkedMvds.add(complementMvd);
			
			PositionListIndex pli = null;
			if (algorithmConfig.isUsePLIs())
				pli = new PositionListIndex(rel.getPLIs(mvd.getLeftHandSide()));
			
			if (isMvdValid(mvd, rel, pli)) {
				foundMvds.add(mvd);
				foundMvds.add(complementMvd);
			}
			
			else if (mvd.getRightHandSide().size() > 1){
				for (int rAtt : mvd.getRightHandSide()){
					List<Integer> newLhs = new ArrayList<Integer>();
					newLhs.addAll(mvd.getLeftHandSide());
					newLhs.add(rAtt);
					List<Integer> newRhs = new ArrayList<Integer>();
					newRhs.addAll(mvd.getRightHandSide());
					newRhs.remove(Integer.valueOf(rAtt));
					MvD newMvd = new MvD(newLhs, newRhs, attributeCount);
					if (!checkedMvds.contains(newMvd) && newMvd.isMinimal(foundMvds))
						mvdsToCheck.add(newMvd);
				}
			}
			
			if (mvd.getRemainingAttributes().size() > 1){
				for (int oAtt : mvd.getRemainingAttributes()){
					List<Integer> newRhs = new ArrayList<Integer>();
					newRhs.addAll(mvd.getRightHandSide());
					newRhs.add(oAtt);
					MvD newMvd = new MvD(mvd.getLeftHandSide(), newRhs, attributeCount);
					if (!checkedMvds.contains(newMvd) && newMvd.isMinimal(foundMvds))
						mvdsToCheck.add(newMvd);
				}
			}
		}
		return foundMvds;
	}
	
	
	public HashSet<MvD> findMvdsTopDownPruning(Relation rel){
		HashSet<MvD> foundMvds = new HashSet<MvD>();
		HashSet<MvD> mvdsToCheck = new HashSet<MvD>();
		HashSet<MvD> checkedMvds = new HashSet<MvD>();
		int attributeCount = rel.getAttributeCount();
		
		List<Integer> attributeList = new ArrayList<Integer>();
		
		for (int i = 0; i < attributeCount; i++){
			attributeList.add(i);
		}
		
		for (int i = 0; i < attributeCount; i++){
			List<Integer> rhs = new ArrayList<Integer>();
			rhs.addAll(attributeList);
			rhs.remove(i);
			
			MvD mvd = new MvD(new ArrayList<Integer>(), rhs, attributeCount);
			mvdsToCheck.add(mvd);
		}
		
		while (!mvdsToCheck.isEmpty()){
			Iterator<MvD> iterator = mvdsToCheck.iterator();
			MvD mvd = iterator.next();
			iterator.remove();
			
			MvD complementMvd = new MvD(mvd.getLeftHandSide(), mvd.getRemainingAttributes(), attributeCount);
			checkedMvds.add(mvd);
			checkedMvds.add(complementMvd);
			
			PositionListIndex pli = null;
			if (algorithmConfig.isUsePLIs())
				pli = new PositionListIndex(rel.getPLIs(mvd.getLeftHandSide()));
			
			if (isMvdValid(mvd, rel, pli)) {
				foundMvds.add(mvd);
				foundMvds.add(complementMvd);
			}
			
			else if (mvd.getRightHandSide().size() > 1){
				for (int rAtt : mvd.getRightHandSide()){
					List<Integer> newLhs = new ArrayList<Integer>();
					newLhs.addAll(mvd.getLeftHandSide());
					newLhs.add(rAtt);
					List<Integer> newRhs = new ArrayList<Integer>();
					newRhs.addAll(mvd.getRightHandSide());
					newRhs.remove(Integer.valueOf(rAtt));
					MvD newMvd = new MvD(newLhs, newRhs, attributeCount);
//					if (newMvd.toString().equals("[0, 1, 8 ->> 2, 4, 5, 6, 7, 9]"))
//						System.out.println("");
					if (!checkedMvds.contains(newMvd) && newMvd.isMinimal(foundMvds))
						mvdsToCheck.add(newMvd);
				}
			}
			
			if (mvd.getRightHandSide().size() > 1){
				for (int rAtt : mvd.getRightHandSide()){
					List<Integer> newRhs = new ArrayList<Integer>();
					newRhs.addAll(mvd.getRightHandSide());
					newRhs.remove(Integer.valueOf(rAtt));
					MvD newMvd = new MvD(mvd.getLeftHandSide(), newRhs, attributeCount);
//					if (newMvd.toString().equals("[0, 1, 8 ->> 2, 4, 5, 6, 7, 9]"))
//						System.out.println("");
					if (!checkedMvds.contains(newMvd) && newMvd.isMinimal(foundMvds))
						mvdsToCheck.add(newMvd);
				}
			}
		}
		return foundMvds;
	}
	
	
	
	public HashSet<MvD> findMvdsLhsFirstPruning(Relation rel){
		HashSet<MvD> foundMvds = new HashSet<MvD>();
		int attributeCount = rel.getAttributeCount();
		
		List<Integer> attributeList = new ArrayList<Integer>();
		
		for (int i = 0; i < attributeCount; i++){
			attributeList.add(i);
		}
		
		for (int lhsSize = 0; lhsSize <= attributeCount - 2; lhsSize++){
			List<List<Integer>> lhsList = getAllSublistsOfSize(attributeList, lhsSize);
			System.out.println(lhsSize);
			for (List<Integer> lhs : lhsList){
//				System.out.println(lhs.toString());
				PositionListIndex pli = null;
				if (algorithmConfig.isUsePLIs())
					pli = new PositionListIndex(rel.getPLIs(lhs));
				
				List<List<Integer>> subsetRhssDirty = new ArrayList<List<Integer>>();
				List<List<Integer>> subsetRhss = new ArrayList<List<Integer>>();
				for (MvD foundMvd : foundMvds){
					if (lhs.containsAll(foundMvd.getLeftHandSide()) && !subsetRhssDirty.contains(foundMvd.getRightHandSide())){
						subsetRhssDirty.add(foundMvd.getRightHandSide());
					}
				}
				
				for (List<Integer> inducedRhs : subsetRhssDirty){
					List<Integer> inducedRhsCopy = new ArrayList<Integer>();
					inducedRhsCopy.addAll(inducedRhs);
					
					for (Integer value : lhs){
						if (inducedRhsCopy.contains(value)){
							inducedRhsCopy.remove(Integer.valueOf(value));
						}
					}
					if (inducedRhs.size() > 0 && !subsetRhss.contains(inducedRhsCopy))
						subsetRhss.add(inducedRhsCopy);
				}
				if (subsetRhss.isEmpty()){
					List<Integer> possibleRhs = new ArrayList<Integer>();
					possibleRhs.addAll(attributeList);
					possibleRhs.removeAll(lhs);
					HashSet<MvD> newlyFoundMvds = findMvdsForLhs(rel, lhs, possibleRhs, pli);
					for (MvD newMvd : newlyFoundMvds)
						if (newMvd.isMinimal(foundMvds) && newMvd.isMinimal(newlyFoundMvds))
							foundMvds.add(newMvd);
				}
				else {
					// remove single element subsetRhss from other subsetRhss
					for (List<Integer> inducedRhs : subsetRhss){
						if (inducedRhs.size() == 1){
							for (List<Integer> inducedRhs2 : subsetRhss){
								if (inducedRhs2.size() > 1){
									inducedRhs2.remove(Integer.valueOf(inducedRhs.get(0)));
								}
							}
						}
					}
					
					// remove duplicates
					List<List<Integer>> subsetRhssNoDupes = new ArrayList<List<Integer>>();
					Iterator<List<Integer>> iterator = subsetRhss.iterator();
					while (iterator.hasNext()){
						List<Integer> next = iterator.next();
						iterator.remove();
						if (!subsetRhss.contains(next))
							subsetRhssNoDupes.add(next);
					}
					subsetRhss = subsetRhssNoDupes;
					
					
					// prepare for splitting of Rhs: deal with overlap between RHS, overlap between RHS and LHS, remove single-column RHS
					List<List<Integer>> cleanSubsetRhss = new ArrayList<List<Integer>>();
//					List<List<Integer>> minimizedInducedRhs = new ArrayList<List<Integer>>();
					for (int i = 0; i < subsetRhss.size(); i++){
						List<Integer> inducedRhs = subsetRhss.get(i);
						boolean overlapFound = false;
						for (int j = i+1; j < subsetRhss.size(); j++){
							List<Integer> otherRhs = subsetRhss.get(j);
							List<Integer> overlap = new ArrayList<Integer>();
							overlap.addAll(inducedRhs);
							overlap.retainAll(otherRhs);
							if (!overlap.isEmpty())
							{
								overlapFound = true;
								// AB ->> CD,EF,GH; AC ->> BDF,EGHI
								// => ABC -> D,E,F,GH,I
								List<Integer> remainder1 = new ArrayList<Integer>();
								remainder1.addAll(inducedRhs);
								remainder1.removeAll(overlap);
								List<Integer> remainder2 = new ArrayList<Integer>();
								remainder2.addAll(otherRhs);
								remainder2.removeAll(overlap);
								
								if (!cleanSubsetRhss.contains(overlap))
									cleanSubsetRhss.add(overlap);
								if (!cleanSubsetRhss.contains(remainder1) && !remainder1.isEmpty())
									cleanSubsetRhss.add(remainder1);
								if (!cleanSubsetRhss.contains(remainder2) && !remainder2.isEmpty())
									cleanSubsetRhss.add(remainder2);
							}
						}
						if (!overlapFound && !inducedRhs.isEmpty()){
							List<Integer> inducedRhsCopy = new ArrayList<Integer>();
							inducedRhsCopy.addAll(inducedRhs);
							cleanSubsetRhss.add(inducedRhsCopy);
						}
					}
					
					subsetRhss.clear();
					
					for (List<Integer> guaranteedRhs : cleanSubsetRhss){
						MvD deducedMvd = new MvD (lhs, guaranteedRhs, attributeCount);
						if (guaranteedRhs.size() > 1){
							HashSet<MvD> newlyFoundMvds = findMvdsForLhs(rel, lhs, guaranteedRhs, pli);
							for (MvD newMvd : newlyFoundMvds)
								if (newMvd.isMinimal(foundMvds) && newMvd.isMinimal(newlyFoundMvds))
									foundMvds.add(newMvd);
						}
						if (deducedMvd.isMinimal(foundMvds))
							foundMvds.add(deducedMvd);
					}
				}
			}
		}
		return foundMvds;
	}
	
	
	public HashSet<MvD> findMvdsForLhs(Relation rel, List<Integer> lhs, List<Integer> possibleRhs, PositionListIndex pli){
		HashSet<MvD> foundMvds = new HashSet<MvD>();
		int attributeCount = rel.getAttributeCount();
		
		List<Integer> possibleRhsCopy = new ArrayList<Integer>();
		possibleRhsCopy.addAll(possibleRhs);
		MvD fullMvd = new MvD(lhs, possibleRhsCopy, attributeCount);
		if (!fullMvd.getRemainingAttributes().isEmpty() && isMvdValid(fullMvd, rel, pli))
			foundMvds.add(fullMvd);
		
		int removedRhs = possibleRhs.get(0);
		boolean wasRemoved = false;
		//Potentially Dangerous?
		if (possibleRhs.size() > 1){
			possibleRhs.remove(0);
			wasRemoved = true;
		}
		
		for (int rhsSize = 1; rhsSize <= possibleRhs.size(); rhsSize++){
			List<List<Integer>> rhsList = getAllSublistsOfSize(possibleRhs, rhsSize);
			
			for (List<Integer> rhs : rhsList){
				MvD mvd = new MvD(lhs, rhs, attributeCount);
				if (isMvdValid(mvd, rel, pli)){
					foundMvds.add(mvd);
					possibleRhs.removeAll(rhs);
					if (wasRemoved)
						possibleRhs.add(removedRhs);
					if (!possibleRhs.isEmpty()){
						possibleRhsCopy = new ArrayList<Integer>();
						possibleRhsCopy.addAll(possibleRhs);
						MvD complementMvd = new MvD(lhs, possibleRhs, attributeCount);
						foundMvds.add(complementMvd);
						foundMvds.addAll(findMvdsForLhs(rel, lhs, possibleRhsCopy, pli));
					}
					
					HashSet<MvD> minimalMvds = new HashSet<MvD>();
					for (MvD foundMvd : foundMvds)
						if (foundMvd.isMinimal(foundMvds))
							minimalMvds.add(foundMvd);
					
					return minimalMvds;
				}
			}
		}
		return foundMvds;
	}
	

}
