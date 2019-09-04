package de.metanome.algorithms.dcfinder;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.algorithm_types.BooleanParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.DenialConstraintAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.IntegerParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.RelationalInputParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.StringParameterAlgorithm;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirement;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementBoolean;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementInteger;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementRelationalInput;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementString;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.DenialConstraintResultReceiver;
import de.metanome.algorithms.dcfinder.denialconstraints.DenialConstraint;
import de.metanome.algorithms.dcfinder.denialconstraints.DenialConstraintSet;
import de.metanome.algorithms.dcfinder.input.Input;
import de.metanome.algorithms.dcfinder.predicates.PredicateBuilder;

public class DCFinderMetanome extends DCFinder implements DenialConstraintAlgorithm, RelationalInputParameterAlgorithm,
		StringParameterAlgorithm, BooleanParameterAlgorithm, IntegerParameterAlgorithm {

	public static final String NO_CROSS_COLUMN = "NO_CROSS_COLUMN";
	public static final String CROSS_COLUMN_STRING_MIN_OVERLAP = "CROSS_COLUMN_STRING_MIN_OVERLAP";
	public static final String APPROXIMATION_DEGREE = "APPROXIMATION_DEGREE";
	public static final String CHUNK_LENGTH = "CHUNK_LENGTH";
	public static final String BUFFER_LENGTH = "BUFFER_LENGTH";
	public static final String INPUT = "INPUT";

	private Boolean noCrossColumn = Boolean.TRUE;
	private double minimumSharedValue = 0.30d;
	
	private DenialConstraintResultReceiver resultReceiver;
	private RelationalInputGenerator inputGen;

	private static Logger log = LoggerFactory.getLogger(DCFinderMetanome.class);

	@Override
	public void execute() throws AlgorithmExecutionException {

		Input input = new Input(inputGen.generateNewCopy());
		PredicateBuilder predicates = new PredicateBuilder(input, noCrossColumn, minimumSharedValue);
		log.info("Size of the predicate space:" + predicates.getPredicates().size());

		DenialConstraintSet dcs = super.run(input, predicates);
		log.info("Result size: " + dcs.size());
		for (DenialConstraint dc : dcs) {
			resultReceiver.receiveResult(dc.toResult());
		}

	}

	@Override
	public ArrayList<ConfigurationRequirement<?>> getConfigurationRequirements() {

		ArrayList<ConfigurationRequirement<?>> requirements = new ArrayList<>();

		ConfigurationRequirementInteger chunkLengthReq = new ConfigurationRequirementInteger(CHUNK_LENGTH);
		chunkLengthReq.setDefaultValues(new Integer[] { Long.valueOf(this.chunkLength).intValue() });
		requirements.add(chunkLengthReq);

		ConfigurationRequirementInteger bufferLengthReq = new ConfigurationRequirementInteger(BUFFER_LENGTH);
		bufferLengthReq.setDefaultValues(new Integer[] { Integer.valueOf(this.bufferLength) });
		requirements.add(bufferLengthReq);

		ConfigurationRequirementString approximationDegreeReq = new ConfigurationRequirementString(
				APPROXIMATION_DEGREE);
		approximationDegreeReq.setDefaultValues(new String[] { "" + this.errorThreshold });
		requirements.add(approximationDegreeReq);

		ConfigurationRequirementBoolean noCrossColumnReq = new ConfigurationRequirementBoolean(NO_CROSS_COLUMN);
		noCrossColumnReq.setDefaultValues(new Boolean[] { this.noCrossColumn });
		requirements.add(noCrossColumnReq);

		ConfigurationRequirementString minOverlapReq = new ConfigurationRequirementString(
				CROSS_COLUMN_STRING_MIN_OVERLAP);
		minOverlapReq.setDefaultValues(new String[] { "" + this.minimumSharedValue });
		requirements.add(minOverlapReq);

		for (ConfigurationRequirement<?> req : requirements) {
			req.setRequired(false);
		}

		requirements.add(new ConfigurationRequirementRelationalInput(INPUT));

		return requirements;
	}

	@Override
	public void setStringConfigurationValue(String identifier, String... values)
			throws AlgorithmConfigurationException {

		if (identifier.equals(CROSS_COLUMN_STRING_MIN_OVERLAP)) {
			try {
				this.minimumSharedValue = Double.parseDouble(values[0]);
			} catch (NumberFormatException ex) {
				throw new AlgorithmConfigurationException("Minimum shared values must be a numeric value");
			}

		} else if (identifier.equals(APPROXIMATION_DEGREE)) {
			try {
				this.errorThreshold = Double.parseDouble(values[0]);
			} catch (NumberFormatException ex) {
				throw new AlgorithmConfigurationException("Minimum shared values must be a numeric value");
			}

		}

		else
			throw new AlgorithmConfigurationException("Unknown string parameter.");

	}

	@Override
	public void setBooleanConfigurationValue(String identifier, Boolean... values)
			throws AlgorithmConfigurationException {
		if (!identifier.equals(NO_CROSS_COLUMN))
			throw new AlgorithmConfigurationException("Unknown boolean parameter.");

		this.noCrossColumn = values[0];
	}

	@Override
	public void setIntegerConfigurationValue(String identifier, Integer... values)
			throws AlgorithmConfigurationException {
		if (identifier.equals(CHUNK_LENGTH)) {
			this.chunkLength = values[0].intValue();
		} else if (identifier.equals(BUFFER_LENGTH)) {
			this.bufferLength = values[0].intValue();
		} else {
			throw new AlgorithmConfigurationException("Unknown integer parameter.");
		}

	}

	@Override
	public void setResultReceiver(DenialConstraintResultReceiver resultReceiver) {
		this.resultReceiver = resultReceiver;
	}

	@Override
	public void setRelationalInputConfigurationValue(String identifier, RelationalInputGenerator... values)
			throws AlgorithmConfigurationException {
		if (!identifier.equals(INPUT))
			throw new AlgorithmConfigurationException("Unknown relational input parameter.");

		this.inputGen = values[0];
	}

	@Override
	public String getAuthors() {
		return "Eduardo Pena, Eduardo Cunha Felix Naumann";
	}

	@Override
	public String getDescription() {
		return "An (approximate/exact) DC discovery method.";
	}
}
