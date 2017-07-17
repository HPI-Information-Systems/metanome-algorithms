package de.metanome.algorithms.dva;




import java.util.ArrayList;
import java.util.List;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.BasicStatisticsResultReceiver;
import de.metanome.algorithm_integration.results.BasicStatistic;
import de.metanome.algorithm_integration.results.basic_statistic_values.BasicStatisticValueLong;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

/**Use Hash Table to calculate the exact distinct value number
 *  @author Hazar.Harmouch**/


public class DVAAlgorithm {

  protected RelationalInputGenerator inputGenerator = null;
  protected BasicStatisticsResultReceiver resultReceiver = null;
  protected final String NUMBEROFDISTINCT = "Number of Distinct Values";
  protected String relationName;
  protected List<String> columnNames;
  protected ArrayList<ObjectOpenHashSet<String>> Columns;
  protected RelationalInput input;
  
  public void execute() throws AlgorithmExecutionException {

    ////////////////////////////////////////////
    // THE DISCOVERY ALGORITHM LIVES HERE :-) //
    ////////////////////////////////////////////
    // initialisation

    input = this.inputGenerator.generateNewCopy();
    this.relationName = input.relationName();
    this.columnNames = input.columnNames();
    Columns=new ArrayList<>();
    for (int i = 0; i < columnNames.size(); i++)
      Columns.add(new ObjectOpenHashSet<String>());
   
      //pass over the data
    while (input.hasNext()) {
      List<String> CurrentTuple=input.next();
      // pass for each column
      for (int i = 0; i < columnNames.size(); i++)
      {String currentvalue=CurrentTuple.get(i);
        if(currentvalue!=null && !currentvalue.trim().isEmpty())
            Columns.get(i).add(CurrentTuple.get(i).trim());
      }
      }
    
    // add the statistic for that column
    for (int i = 0; i < columnNames.size(); i++)
    addStatistic(NUMBEROFDISTINCT, Columns.get(i).size(), columnNames.get(i), relationName);   
  }



  private void addStatistic(String StatisticName, long Value, String ColumnName,
      String RelationName) throws AlgorithmExecutionException {
    BasicStatistic result = new BasicStatistic(new ColumnIdentifier(RelationName, ColumnName));
    result.addStatistic(StatisticName, new BasicStatisticValueLong(Value));
  System.out.println(StatisticName + " of " + ColumnName + " : " + Value);
    resultReceiver.receiveResult(result);
  }
}
