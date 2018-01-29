package de.hpi.is.md.hybrid;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import de.hpi.is.md.SupportCalculator;
import de.hpi.is.md.hybrid.impl.infer.LhsRhsDisjointnessFilter;
import de.hpi.is.md.hybrid.impl.infer.SpecializationFilter;
import de.hpi.is.md.impl.SizeBasedSupportCalculator;
import de.hpi.is.md.relational.Relation;
import de.hpi.is.md.util.Hashable;
import de.hpi.is.md.util.Hasher;
import java.io.Serializable;
import lombok.Data;
import lombok.NonNull;

@Data
public class DiscoveryConfiguration implements Serializable {

	private static final HashFunction HASH_FUNCTION = Hashing.murmur3_128();
	private static final long serialVersionUID = -5704352473375302743L;
	@NonNull
	private SupportCalculator supportCalculator = new SizeBasedSupportCalculator();
	@NonNull
	private SpecializationFilter specializationFilter = LhsRhsDisjointnessFilter.INSTANCE;
	@NonNull
	private LevelBundle levelBundle = getDefaultLevelBundle();
	private double minThreshold = 0.7;

	private static LevelBundle getDefaultLevelBundle() {
		return HyMDProperties.isSamplingEnabled() ? LevelBundle.CARDINALITY : LevelBundle.DISTANCE;
	}

	public HashCode hash(Relation left, Relation right, MDMapping mapping) {
		long minSupport = supportCalculator.calculateSupport(left, right);
		return hash(left, right, mapping, minSupport);
	}

	public HashCode hash(Relation relation, MDMapping mapping) {
		long minSupport = supportCalculator.calculateSupport(relation);
		return hash(relation, relation, mapping, minSupport);
	}

	private HashCode hash(Hashable left, Hashable right, MDMapping mapping, long minSupport) {
		return Hasher.of(HASH_FUNCTION)
			.put(left)
			.put(right)
			.put(mapping)
			.putDouble(minThreshold)
			.putLong(minSupport)
			.put(specializationFilter)
			.hash();
	}
}
