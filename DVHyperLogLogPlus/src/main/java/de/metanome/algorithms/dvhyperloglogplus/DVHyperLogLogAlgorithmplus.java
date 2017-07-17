package de.metanome.algorithms.dvhyperloglogplus;

import java.util.ArrayList;
import java.util.List;

import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.BasicStatisticsResultReceiver;
import de.metanome.algorithm_integration.results.BasicStatistic;
import de.metanome.algorithm_integration.results.basic_statistic_values.BasicStatisticValueLong;
/**
* Implementation of HyperLogLogplus 
* Reference: Heule, S., Nunkesser, M., & Hall, A. (2013, March).
* HyperLogLog in practice: algorithmic engineering of a state of the art cardinality estimation
* algorithm. In Proceedings of the 16th International Conference on Extending Database Technology
* (pp. 683-692). ACM.Hyperloglog. Flajolet, P., Fusy, É., Gandouet, O., & Meunier, F. (2008).
* Hyperloglog: the analysis of a near-optimal cardinality estimation algorithm. DMTCS Proceedings,
* (1). 
* * @author Hazar.Harmouch 
*/
public class DVHyperLogLogAlgorithmplus {
	
	protected RelationalInputGenerator inputGenerator = null;
	protected BasicStatisticsResultReceiver resultReceiver = null;
	
	protected String relationName;
	protected List<String> columnNames;
	private final String NUMBEROFDISTINCT = "Number of Distinct Values";
	private RelationalInput input;
	protected int p=14;
	protected int ps=25;
	public void execute() throws AlgorithmExecutionException{
		////////////////////////////////////////////
		// THE DISCOVERY ALGORITHM LIVES HERE :-) //
		////////////////////////////////////////////
		
	
	input = this.inputGenerator.generateNewCopy();
    this.relationName = input.relationName();
    this.columnNames = input.columnNames();
    ArrayList<HyperLogLogPlus> Columns = new ArrayList<HyperLogLogPlus>();
    for (int i = 0; i < columnNames.size(); i++)
      Columns.add(new HyperLogLogPlus(p,ps));
     
    while (input.hasNext()) {
      List<String> CurrentTuple=input.next();
      for (int i = 0; i < columnNames.size(); i++)
        if(CurrentTuple.get(i)!=(null))
           Columns.get(i).offer(CurrentTuple.get(i));
      
		
	}
  
    for (int i = 0; i < columnNames.size(); i++)
    {addStatistic(NUMBEROFDISTINCT, Columns.get(i).cardinality(), columnNames.get(i), relationName); }
    
	}
	
	 private void addStatistic(String StatisticName, long Value, String ColumnName,
         String RelationName) throws AlgorithmExecutionException {
       BasicStatistic result = new BasicStatistic(new ColumnIdentifier(RelationName, ColumnName));
       result.addStatistic(StatisticName, new BasicStatisticValueLong(Value));
      //System.out.println(StatisticName + " of " + ColumnName + " : " + Value);
       resultReceiver.receiveResult(result);
     }
	
}
