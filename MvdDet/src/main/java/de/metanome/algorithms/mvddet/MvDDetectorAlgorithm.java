package de.metanome.algorithms.mvddet;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.ColumnCombination;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.algorithm_types.DatabaseConnectionParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.FileInputParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.MultivaluedDependencyAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.StringParameterAlgorithm;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirement;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementDatabaseConnection;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementFileInput;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementString;
import de.metanome.algorithm_integration.input.DatabaseConnectionGenerator;
import de.metanome.algorithm_integration.input.FileInputGenerator;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.ColumnNameMismatchException;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.result_receiver.MultivaluedDependencyResultReceiver;
import de.metanome.algorithm_integration.results.MultivaluedDependency;


public class MvDDetectorAlgorithm
	implements MultivaluedDependencyAlgorithm, StringParameterAlgorithm, FileInputParameterAlgorithm, DatabaseConnectionParameterAlgorithm {
	
	public final static String LISTBOX_IDENTIFIER = "column names";
	public final static String CHECKBOX_IDENTIFIER = "column names";
	public final static String STRING_IDENTIFIER = "pathToOutputFile";
	public final static String CSVFILE_IDENTIFIER = "input file";
	public final static String DATABASE_IDENTIFIER = "DB-connection";
	protected String path = null;
	protected String selectedColumn = null;
//	protected DatabaseConnectionGenerator inputGenerator;
	protected RelationalInputGenerator inputGenerator = null;
	protected MultivaluedDependencyResultReceiver resultReceiver = null;
	
	protected String relationName;
	protected List<String> columnNames;
	protected MvDAlgorithmConfig algorithmConfig = null;
	
	public void execute() throws AlgorithmExecutionException {
		
		////////////////////////////////////////////
		// THE DISCOVERY ALGORITHM LIVES HERE :-) //
		////////////////////////////////////////////
		this.initialize();
		List<List<String>> records = this.readInput();
		List<MultivaluedDependency> results;
		try {
			results = this.generateResults(records, algorithmConfig);
			this.emit(results);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		/////////////////////////////////////////////
		
	}
	
	protected void initialize() throws InputGenerationException, AlgorithmConfigurationException {
		RelationalInput input = this.inputGenerator.generateNewCopy();
		this.relationName = input.relationName();
		this.columnNames = input.columnNames();
//		this.algorithmConfig = new MvDAlgorithmConfig();
	}
	
	protected List<List<String>> readInput() throws InputGenerationException, AlgorithmConfigurationException, InputIterationException {
		List<List<String>> records = new ArrayList<>();
		RelationalInput input = this.inputGenerator.generateNewCopy();
		while (input.hasNext())
			records.add(input.next());
		return records;
	}
	
	protected void print(List<List<String>> records) {
		
		// Print schema
		System.out.print(this.relationName + "( ");
		for (String columnName : this.columnNames)
			System.out.print(columnName + " ");
		System.out.println(")");
		
		// Print records
		for (List<String> record : records) {
			System.out.print("| ");
			for (String value : record)
				System.out.print(value + " | ");
			System.out.println();
		}
	}
	
	protected List<MultivaluedDependency> generateResults(List<List<String>> records, MvDAlgorithmConfig algorithmConfig) throws AlgorithmExecutionException, FileNotFoundException {
		List<MultivaluedDependency> results = new ArrayList<MultivaluedDependency>();
		
		MvdFinder mvdFinder = new MvdFinder();
		List<MvD> internalMvds = mvdFinder.findMvDs(records, algorithmConfig);
		
		for (MvD internalMvd : internalMvds){
			ColumnCombination lhs = new ColumnCombination();
			Set<ColumnIdentifier> identifiers = new HashSet<ColumnIdentifier>();
			for (int index : internalMvd.getLeftHandSide())
				identifiers.add(new ColumnIdentifier(this.relationName, this.columnNames.get(index)));
			lhs.setColumnIdentifiers(identifiers);
			
			ColumnCombination rhs = new ColumnCombination();
			identifiers = new HashSet<ColumnIdentifier>();
			for (int index : internalMvd.getRightHandSide())
				identifiers.add(new ColumnIdentifier(this.relationName, this.columnNames.get(index)));
			rhs.setColumnIdentifiers(identifiers);
			
			MultivaluedDependency mvd = new MultivaluedDependency(lhs, rhs);
			results.add(mvd);
		}
		
		return results;
	}
	
	protected ColumnIdentifier getRandomColumn() {
		Random random = new Random(System.currentTimeMillis());
		return new ColumnIdentifier(this.relationName, this.columnNames.get(random.nextInt(this.columnNames.size())));
	}
	
	protected void emit(List<MultivaluedDependency> results) throws CouldNotReceiveResultException, ColumnNameMismatchException {
		for (MultivaluedDependency mvd : results)
			this.resultReceiver.receiveResult(mvd);
		
		System.out.println("\n\n\n" + results.size() + " results emitted.\n\n\n");
	}
	
	@Override
	public String toString() {
		return this.getClass().getName();
	}

	@Override
	public ArrayList<ConfigurationRequirement<?>> getConfigurationRequirements() {
		ArrayList<ConfigurationRequirement<?>> configurationRequirement = new ArrayList<>();

	    ConfigurationRequirementString requirementString =
	        new ConfigurationRequirementString(STRING_IDENTIFIER);
	    ConfigurationRequirementFileInput requirementFileInput =
	        new ConfigurationRequirementFileInput(CSVFILE_IDENTIFIER);
	    ConfigurationRequirementDatabaseConnection requirementDatabaseConnection =
	        new ConfigurationRequirementDatabaseConnection(DATABASE_IDENTIFIER);
	    
	    requirementString.setRequired(false);
	    requirementFileInput.setRequired(false);
	    requirementDatabaseConnection.setRequired(false);

	    configurationRequirement.add(requirementString);
	    configurationRequirement.add(requirementFileInput);
	    configurationRequirement.add(requirementDatabaseConnection);
	    
		return configurationRequirement;
	}

	@Override
	public String getAuthors() {
		return "Tim Draeger";
	}

	@Override
	public String getDescription() {
		return "Algorithm for MvD Detection using a Left-Hand-Side-First approach.";
	}

	@Override
	public void setDatabaseConnectionGeneratorConfigurationValue(String identifier,
			DatabaseConnectionGenerator... values) throws AlgorithmConfigurationException {

		    if (!identifier.equals(DATABASE_IDENTIFIER)) {
		      throw new AlgorithmConfigurationException("Incorrect identifier or value list length.");
		    }
		
	}

	@Override
	public void setFileInputConfigurationValue(String identifier, FileInputGenerator... values)
			throws AlgorithmConfigurationException {
		if (!identifier.equals(CSVFILE_IDENTIFIER)) {
		      throw new AlgorithmConfigurationException("Incorrect identifier or value list length.");
		    }
	}

	@Override
	public void setStringConfigurationValue(String identifier, String... values)
			throws AlgorithmConfigurationException {
		if (!identifier.equals(STRING_IDENTIFIER)) {
		      throw new AlgorithmConfigurationException("Incorrect identifier or value list length.");
		    }
	}

	@Override
	public void setResultReceiver(MultivaluedDependencyResultReceiver resultReceiver) {
		this.resultReceiver = resultReceiver;
		
	}
}
