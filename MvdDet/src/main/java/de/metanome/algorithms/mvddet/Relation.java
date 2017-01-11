package de.metanome.algorithms.mvddet;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Relation {
	private List<StringTuple> stringTuples;
	private List<Tuple> tuples;
	private HashSet<Tuple> tupleSet;
	private int attributeCount;
	private int tupleCount;
	private String name;
	private List<PositionListIndex> positionListIndices;
	
	public Relation(int numberOfAttributes) {
		stringTuples = new ArrayList<StringTuple>();
		attributeCount = numberOfAttributes;
		tupleCount = 0;
		positionListIndices = new ArrayList<PositionListIndex>();
	}
	
	public Relation(String path, String seperator){
		stringTuples = new ArrayList<StringTuple>();
		positionListIndices = new ArrayList<PositionListIndex>();
		try(BufferedReader br = new BufferedReader(new FileReader(path))) {
		    String line = br.readLine();
		    if (line != null)
		    	attributeCount = line.split(seperator).length;

		    while (line != null) {
		    	addTuple(new StringTuple(line, seperator));
		        line = br.readLine();
		    }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Relation(List<List<String>> records){
		stringTuples = new ArrayList<StringTuple>();
		attributeCount = records.get(0).size();
		tupleCount = 0;
		positionListIndices = new ArrayList<PositionListIndex>();
		for (List<String> tupleList : records){
			addTuple(new StringTuple(tupleList));
		}
	}
	
	public void addTuple(StringTuple tuple){
		if (tuple.size() != attributeCount)
			System.out.println("Wrong number of attributes in tuple " + tuple + ".\n"
					+ "Expected: " + attributeCount + "; Found: " + tuple.size());
		stringTuples.add(tuple);
		tupleCount += 1;
	}
	
	public String toString(){
		String returnString = "";
		
		for (StringTuple tuple : stringTuples){
			returnString += "[" + tuple.toString() + "]\n";
		}
		
		return returnString;
	}
	
	public String toIntString(){
		String returnString = "";
		
		for (Tuple tuple : tuples){
			returnString += "[" + tuple.toString() + "]\n";
		}
		
		return returnString;
	}
	
	public int getAttributeCount(){
		return attributeCount;
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String nameString){
		name = nameString;
	}
	
	public int getTupleCount(){
		return tupleCount;
	}
	
	public Tuple getTupleAt(int position){
		return tuples.get(position);
	}
	
	public StringTuple getStringTupleAt(int position){
		return stringTuples.get(position);
	}
	
	public boolean doesTupleExist(Tuple tuple){
		return tuples.contains(tuple);
	}
	
	public boolean doesTupleExist(StringTuple tuple){
		return stringTuples.contains(tuple);
	}
	
	public boolean doesTupleExist(Tuple tuple, List<Integer> tupleIDs){
		for (Integer tupleID : tupleIDs)
			if (tuples.get(tupleID).equals(tuple))
				return true;
		return false;
	}
	
	public boolean doesTupleExist(StringTuple tuple, List<Integer> tupleIDs){
		for (Integer tupleID : tupleIDs)
			if (stringTuples.get(tupleID).equals(tuple))
				return true;
		return false;
	}
	
	public PositionListIndex getPLI(int column){
		return positionListIndices.get(column);
	}
	
	public List<PositionListIndex> getPLIs(List<Integer> columns){
		List<PositionListIndex> returnPLIs = new ArrayList<PositionListIndex>();
		if (columns.isEmpty()){
			List<Set<Integer>> pliContent = new ArrayList<Set<Integer>>();
			Set<Integer> allRows = new HashSet<Integer>();
			for (int i = 0; i < tupleCount; i++)
				allRows.add(i);
			pliContent.add(allRows);
			returnPLIs.add(new PositionListIndex(pliContent));
		}
		for (int column : columns)
			returnPLIs.add(getPLI(column));
		return returnPLIs;
	}
	
	public void removeDuplicates(){		
		for (int i = 0; i < stringTuples.size() - 1; i++){
			StringTuple t1 = stringTuples.get(i);
			for (int j = i+1; j < stringTuples.size(); j += 0){
				StringTuple t2 = stringTuples.get(j);
				if (t1.equals(t2)) {
					stringTuples.remove(j);
					tupleCount --;
				}
				else
					j++;
			}
		}
	}
	
	
	
	public void ConvertToIntTuples(){
		List<Map<String, Integer>> maps = new ArrayList<Map<String, Integer>>();
		for (int i = 0; i < attributeCount; i++){
			maps.add(new HashMap<String, Integer>());
		}
		for (int j = 0; j < stringTuples.size(); j++){
			StringTuple stringTuple = stringTuples.get(j);
			for (int i = 0; i < attributeCount; i++){
				String value = stringTuple.getValueAt(i);
				if (maps.get(i).containsKey(value)){
					maps.get(i).put(value, maps.get(i).get(value)+1);
				}
				else{
					maps.get(i).put(value, 1);
				}
			}
		}
		
		for (int i = 0; i < attributeCount; i++){
			int j = 1;
			for (Entry<String, Integer> entry : maps.get(i).entrySet()){
				entry.setValue(j);
				j++;
				
			}
		}
		
		tuples = new ArrayList<Tuple>();
		tupleSet = new HashSet<Tuple>();
		for (StringTuple stringTuple: stringTuples){
			int[] tupleValues = new int[attributeCount];
			for (int i = 0; i < attributeCount; i++){
				String valueString = stringTuple.getValueAt(i);
				tupleValues[i] = maps.get(i).get(valueString);
			}
			Tuple tuple = new Tuple(tupleValues);
			tuples.add(tuple);
		}
		stringTuples = null;
	}
	
	
	public void ConvertToIntTuplesUnique(){
		List<Map<String, Integer>> maps = new ArrayList<Map<String, Integer>>();
		for (int i = 0; i < attributeCount; i++){
			maps.add(new HashMap<String, Integer>());
		}
		for (int j = 0; j < stringTuples.size(); j++){
			StringTuple stringTuple = stringTuples.get(j);
			for (int i = 0; i < attributeCount; i++){
				String value = stringTuple.getValueAt(i);
				if (maps.get(i).containsKey(value)){
					maps.get(i).put(value, maps.get(i).get(value)+1);
				}
				else{
					maps.get(i).put(value, 1);
				}
			}
		}
		
		for (int i = 0; i < attributeCount; i++){
			int j = 1;
			for (Entry<String, Integer> entry : maps.get(i).entrySet()){
				if (entry.getValue() == 1){
					entry.setValue(0);
				}
				else{
					entry.setValue(j);
					j++;
				}
			}
		}
		
		tuples = new ArrayList<Tuple>();
		tupleSet = new HashSet<Tuple>();
		for (StringTuple stringTuple: stringTuples){
			int[] tupleValues = new int[attributeCount];
			for (int i = 0; i < attributeCount; i++){
				String valueString = stringTuple.getValueAt(i);
				tupleValues[i] = maps.get(i).get(valueString);
			}
			Tuple tuple = new Tuple(tupleValues);
			tuples.add(tuple);
		}
		stringTuples = null;
	}
	
	
	public void ConvertToIntTuplesPLI(){
		List<Map<String, Integer>> maps = new ArrayList<Map<String, Integer>>();
		List<Map<Integer, Set<Integer>>> pliMaps = new ArrayList<Map<Integer, Set<Integer>>>();
		for (int i = 0; i < attributeCount; i++){
			maps.add(new HashMap<String, Integer>());
			pliMaps.add(new HashMap<Integer, Set<Integer>>());
		}
		for (int j = 0; j < stringTuples.size(); j++){
			StringTuple stringTuple = stringTuples.get(j);
			for (int i = 0; i < attributeCount; i++){
				String value = stringTuple.getValueAt(i);
				if (maps.get(i).containsKey(value)){
					maps.get(i).put(value, maps.get(i).get(value)+1);
				}
				else{
					maps.get(i).put(value, 1);
				}
			}
		}
		
		for (int i = 0; i < attributeCount; i++){
			int j = 1;
			for (Entry<String, Integer> entry : maps.get(i).entrySet()){
				entry.setValue(j);
				j++;
				
			}
		}
		
		tuples = new ArrayList<Tuple>();
		tupleSet = new HashSet<Tuple>();
		for (StringTuple stringTuple: stringTuples){
			int[] tupleValues = new int[attributeCount];
			for (int i = 0; i < attributeCount; i++){
				String valueString = stringTuple.getValueAt(i);
				tupleValues[i] = maps.get(i).get(valueString);
			}
			Tuple tuple = new Tuple(tupleValues);
			tuples.add(tuple);
//			tupleSet.add(tuple);
		}
		stringTuples = null;
		
		for (int j = 0; j < tuples.size(); j++){
			Tuple tuple = tuples.get(j);
			for (int i = 0; i < attributeCount; i++){
				int value = tuple.getValueAt(i);
				if (value != 0){
					if (!pliMaps.get(i).containsKey(value)){
						pliMaps.get(i).put(value, new HashSet<Integer>());
					}
					pliMaps.get(i).get(value).add(j);
				}
			}
		}
		
		for (int i = 0; i < attributeCount; i++){
			if (pliMaps.get(i).size() > 1)
			positionListIndices.add(new PositionListIndex(pliMaps.get(i).values()));
		}
	}
	
	
	public void ConvertToIntTuplesUniquePLI(){
		List<Map<String, Integer>> maps = new ArrayList<Map<String, Integer>>();
		List<Map<Integer, Set<Integer>>> pliMaps = new ArrayList<Map<Integer, Set<Integer>>>();
		for (int i = 0; i < attributeCount; i++){
			maps.add(new HashMap<String, Integer>());
			pliMaps.add(new HashMap<Integer, Set<Integer>>());
		}
		for (int j = 0; j < stringTuples.size(); j++){
			StringTuple stringTuple = stringTuples.get(j);
			for (int i = 0; i < attributeCount; i++){
				String value = stringTuple.getValueAt(i);
				if (maps.get(i).containsKey(value)){
					maps.get(i).put(value, maps.get(i).get(value)+1);
				}
				else{
					maps.get(i).put(value, 1);
				}
			}
		}
		
		for (int i = 0; i < attributeCount; i++){
			int j = 1;
			for (Entry<String, Integer> entry : maps.get(i).entrySet()){
				if (entry.getValue() == 1){
					entry.setValue(0);
				}
				else{
					entry.setValue(j);
					j++;
				}
			}
		}
		
		tuples = new ArrayList<Tuple>();
		tupleSet = new HashSet<Tuple>();
		for (StringTuple stringTuple: stringTuples){
			int[] tupleValues = new int[attributeCount];
			for (int i = 0; i < attributeCount; i++){
				String valueString = stringTuple.getValueAt(i);
				tupleValues[i] = maps.get(i).get(valueString);
			}
			Tuple tuple = new Tuple(tupleValues);
			tuples.add(tuple);
//			tupleSet.add(tuple);
		}
		stringTuples = null;
		
		for (int j = 0; j < tuples.size(); j++){
			Tuple tuple = tuples.get(j);
			for (int i = 0; i < attributeCount; i++){
				int value = tuple.getValueAt(i);
				if (value != 0){
					if (!pliMaps.get(i).containsKey(value)){
						pliMaps.get(i).put(value, new HashSet<Integer>());
					}
					pliMaps.get(i).get(value).add(j);
				}
			}
		}
		
		for (int i = 0; i < attributeCount; i++){
			positionListIndices.add(new PositionListIndex(pliMaps.get(i).values()));
		}
	}
	
	
	
	public void ConvertToUniquePLI(){
		List<Map<String, Set<Integer>>> pliMaps = new ArrayList<Map<String, Set<Integer>>>();
		for (int i = 0; i < attributeCount; i++){
			pliMaps.add(new HashMap<String, Set<Integer>>());
		}
		
		for (int j = 0; j < stringTuples.size(); j++){
			StringTuple tuple = stringTuples.get(j);
			for (int i = 0; i < attributeCount; i++){
				String value = tuple.getValueAt(i);
				if (!pliMaps.get(i).containsKey(value)){
					pliMaps.get(i).put(value, new HashSet<Integer>());
				}
				pliMaps.get(i).get(value).add(j);
			}
		}
		
		for (int j = 0; j < stringTuples.size(); j++){
			StringTuple tuple = stringTuples.get(j);
			for (int i = 0; i < attributeCount; i++){
				if (pliMaps.get(i).get(tuple.getValueAt(i)).size() == 1){
					tuple.setValueAt(i, "_MVD_DETECTOR_UNIQUE_VALUE_");
					tuple.setContainsUniqueValue(true);
					stringTuples.set(j, tuple);
				}
			}
		}
		
		for (int i = 0; i < attributeCount; i++){
			positionListIndices.add(new PositionListIndex(pliMaps.get(i).values()));
		}
	}
	
	
	
	public void ConvertToPLI(){
		List<Map<String, Set<Integer>>> pliMaps = new ArrayList<Map<String, Set<Integer>>>();
		for (int i = 0; i < attributeCount; i++){
			pliMaps.add(new HashMap<String, Set<Integer>>());
		}
		
		for (int j = 0; j < stringTuples.size(); j++){
			StringTuple tuple = stringTuples.get(j);
			for (int i = 0; i < attributeCount; i++){
				String value = tuple.getValueAt(i);
				if (!pliMaps.get(i).containsKey(value)){
					pliMaps.get(i).put(value, new HashSet<Integer>());
				}
				pliMaps.get(i).get(value).add(j);
			}
		}
		
		for (int i = 0; i < attributeCount; i++){
			positionListIndices.add(new PositionListIndex(pliMaps.get(i).values()));
		}
	}
	
	
	
	public void ConvertToUnique(){
		List<Map<String, Set<Integer>>> pliMaps = new ArrayList<Map<String, Set<Integer>>>();
		for (int i = 0; i < attributeCount; i++){
			pliMaps.add(new HashMap<String, Set<Integer>>());
		}
		
		for (int j = 0; j < stringTuples.size(); j++){
			StringTuple tuple = stringTuples.get(j);
			for (int i = 0; i < attributeCount; i++){
				String value = tuple.getValueAt(i);
				if (!pliMaps.get(i).containsKey(value)){
					pliMaps.get(i).put(value, new HashSet<Integer>());
				}
				pliMaps.get(i).get(value).add(j);
			}
		}
		
		for (int j = 0; j < stringTuples.size(); j++){
			StringTuple tuple = stringTuples.get(j);
			for (int i = 0; i < attributeCount; i++){
				if (pliMaps.get(i).get(tuple.getValueAt(i)).size() == 1){
					tuple.setValueAt(i, "_MVD_DETECTOR_UNIQUE_VALUE_");
					tuple.setContainsUniqueValue(true);
					stringTuples.set(j, tuple);
				}
			}
		}
	}
	
	public int countUniqueValues(){
		if (tuples == null)
			return 0;
		int count = 0;
		for (Tuple tuple : tuples){
			for (int i = 0; i < this.attributeCount; i++)
				if (tuple.getValueAt(i) == 0)
					count++;
		}
		return count;
	}
	
	
}
