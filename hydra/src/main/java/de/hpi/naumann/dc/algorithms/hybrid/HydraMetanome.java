package de.hpi.naumann.dc.algorithms.hybrid;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hpi.naumann.dc.denialcontraints.DenialConstraint;
import de.hpi.naumann.dc.denialcontraints.DenialConstraintSet;
import de.hpi.naumann.dc.input.Input;
import de.hpi.naumann.dc.predicates.PredicateBuilder;
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

public class HydraMetanome extends Hydra implements DenialConstraintAlgorithm, IntegerParameterAlgorithm,
		StringParameterAlgorithm, RelationalInputParameterAlgorithm, BooleanParameterAlgorithm {
	public static final String SAMPLE_ROUNDS = "SAMPLE_ROUNDS";
	public static final String EFFICIENCY_THRESHOLD = "EFFICIENCY_THRESHOLD";
	public static final String NO_CROSS_COLUMN = "NO_CROSS_COLUMN";
	public static final String CROSS_COLUMN_STRING_MIN_OVERLAP = "CROSS_COLUMN_STRING_MIN_OVERLAP";
	public static final String INPUT = "INPUT";

	private DenialConstraintResultReceiver resultReceiver;
	private RelationalInputGenerator inputGen;

	private Boolean noCrossColumn = Boolean.TRUE;
	private double minimumSharedValue = 0.15d;

	@Override
	public ArrayList<ConfigurationRequirement<?>> getConfigurationRequirements() {
		ArrayList<ConfigurationRequirement<?>> reqs = new ArrayList<>();

		ConfigurationRequirementInteger sampleReq = new ConfigurationRequirementInteger(SAMPLE_ROUNDS);
		sampleReq.setDefaultValues(new Integer[] { Integer.valueOf(this.sampleRounds) });
		reqs.add(sampleReq);
		ConfigurationRequirementString effThresReq = new ConfigurationRequirementString(EFFICIENCY_THRESHOLD);
		effThresReq.setDefaultValues(new String[] { "" + this.efficiencyThreshold });
		reqs.add(effThresReq);
		ConfigurationRequirementBoolean noCrossColumnReq = new ConfigurationRequirementBoolean(NO_CROSS_COLUMN);
		noCrossColumnReq.setDefaultValues(new Boolean[] { this.noCrossColumn });
		reqs.add(noCrossColumnReq);

		ConfigurationRequirementString minOverlapReq = new ConfigurationRequirementString(CROSS_COLUMN_STRING_MIN_OVERLAP);
		minOverlapReq.setDefaultValues(new String[] { "" + this.minimumSharedValue } );
		reqs.add(minOverlapReq);
		
		for (ConfigurationRequirement<?> req : reqs) {
			req.setRequired(false);
		}
		
		reqs.add(new ConfigurationRequirementRelationalInput(INPUT));
		return reqs;
	}

	@Override
	public void execute() throws AlgorithmExecutionException {
		Input input = new Input(inputGen.generateNewCopy());
		PredicateBuilder predicates = new PredicateBuilder(input, noCrossColumn, minimumSharedValue);
		log.info("Predicate space size:" + predicates.getPredicates().size());

		DenialConstraintSet dcs = super.run(input, predicates);
		log.info("Result size: " + dcs.size());
		for (DenialConstraint dc : dcs) {
			resultReceiver.receiveResult(dc.toResult());
		}
	}

	@Override
	public String getAuthors() {
		return "Tobias Bleifuß, Sebastian Kruse, Felix Naumann";
	}

	@Override
	public String getDescription() {
		return "A hybrid DC discovery method.";
	}

	@Override
	public void setResultReceiver(DenialConstraintResultReceiver resultReceiver) {
		this.resultReceiver = resultReceiver;
	}

	@Override
	public void setIntegerConfigurationValue(String identifier, Integer... values)
			throws AlgorithmConfigurationException {
		if (!identifier.equals(SAMPLE_ROUNDS))
			throw new AlgorithmConfigurationException("Unknown integer parameter.");

		this.sampleRounds = values[0].intValue();
	}

	@Override
	public void setStringConfigurationValue(String identifier, String... values)
			throws AlgorithmConfigurationException {
		if (identifier.equals(EFFICIENCY_THRESHOLD)) {
			try {
				this.efficiencyThreshold = Double.parseDouble(values[0]);
			} catch (NumberFormatException ex) {
				throw new AlgorithmConfigurationException("Efficiency threshold must be a numeric value");
			}

		} else if (identifier.equals(CROSS_COLUMN_STRING_MIN_OVERLAP)) {
			try {
				this.minimumSharedValue = Double.parseDouble(values[0]);
			} catch (NumberFormatException ex) {
				throw new AlgorithmConfigurationException("Minimum shared values must be a numeric value");
			}

		} else
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
	public void setRelationalInputConfigurationValue(String identifier, RelationalInputGenerator... values)
			throws AlgorithmConfigurationException {
		if (!identifier.equals(INPUT))
			throw new AlgorithmConfigurationException("Unknown relational input parameter.");
		
		this.inputGen = values[0];
	}

	private static Logger log = LoggerFactory.getLogger(HydraMetanome.class);
}
