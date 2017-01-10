package de.metanome.algorithms.mvddet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PositionListIndex {
	private List<Set<Integer>> indices = new ArrayList<Set<Integer>>();
	private boolean hadSingleElement = false;
	
	public PositionListIndex(Collection<Set<Integer>> collection) {
		for (Set<Integer> element : collection){
			if (element.size() > 1)
				indices.add(element);
			else
				hadSingleElement = true;
		}
	}
	
	public PositionListIndex(PositionListIndex pli1, PositionListIndex pli2){
		for (Set<Integer> set1 : pli1.getIndices()){
			for (Set<Integer> set2 : pli2.getIndices()){
				Set<Integer> intersection = new HashSet<Integer>();
				intersection.addAll(set1);
				intersection.retainAll(set2);
				if (intersection.size() == 1)
					hadSingleElement = true;
				else if (intersection.size() > 1)
					indices.add(intersection);
			}
		}
	}
	
	public PositionListIndex(List<PositionListIndex> plis){
		if (plis.size() == 0)
			return;
		else if (plis.size() == 1)
			indices = plis.get(0).getIndices();
		else {
			PositionListIndex joinedPLI = plis.get(0);
			for (int i = 1; i < plis.size(); i++){
				joinedPLI = new PositionListIndex(joinedPLI, plis.get(i));
				if (joinedPLI.hadSingleElement())
					hadSingleElement = true;
			}
			indices = joinedPLI.getIndices();
		}
	}

	public List<Set<Integer>> getIndices() {
		return indices;
	}
	
	public List<List<Integer>> getIndicesList() {
		List<List<Integer>> indicesList = new ArrayList<List<Integer>>();
		for (Set<Integer> singleSet : indices){
			List<Integer> singleList = new ArrayList<Integer>();
			singleList.addAll(singleSet);
			indicesList.add(singleList);
		}
		return indicesList;
	}
	
	public boolean hadSingleElement() {
		return hadSingleElement;
	}
	
	public String toString(){
		String  s = "[";
		
		for (Set<Integer> set : indices){
			s+="[";
			s+=set.toString();
			s+="], ";
		}
		
		s+="]";
		
		return s;
	}
	
	
}
