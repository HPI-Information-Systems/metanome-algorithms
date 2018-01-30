package de.hpi.is.md.hybrid.impl.lattice.md;

import com.codahale.metrics.annotation.Timed;
import de.hpi.is.md.hybrid.Lattice;
import de.hpi.is.md.hybrid.SimilaritySet;
import de.hpi.is.md.hybrid.md.MD;
import de.hpi.is.md.hybrid.md.MDSite;
import de.hpi.is.md.util.StreamUtils;
import java.util.Collection;
import java.util.Optional;
import lombok.Getter;
import lombok.NonNull;

//@Metrics
public class LatticeImpl implements Lattice {

	private final Node root;
	private final LevelFunction levelFunction;
	@Getter
	private int depth = 0;

	public LatticeImpl(@NonNull LevelFunction levelFunction) {
		this.levelFunction = levelFunction;
		this.root = new Node(levelFunction.size());
	}

	@Timed
	@Override
	public LatticeMD add(MD md) {
		updateMaxLevel(md);
		LhsRhsPair pair = root.add(md, 0);
		return asGrouped(pair);
	}

	@Timed
	@Override
	public Optional<LatticeMD> addIfMinimal(MD md) {
		updateMaxLevel(md);
		return root.addIfMinimal(md, 0)
			.map(this::asGrouped);
	}

	@Timed
	@Override
	public boolean containsMdOrGeneralization(MD md) {
		return root.containsMdOrGeneralization(md, 0);
	}

	@Timed
	@Override
	public Collection<LatticeMD> findViolated(SimilaritySet similaritySet) {
		Collection<LhsRhsPair> violated = ViolatedMDFinder.create(similaritySet)
			.find(root);
		return StreamUtils.seq(violated)
			.map(this::asGrouped)
			.toList();
	}

	@Timed
	@Override
	public Collection<LatticeMD> getLevel(int level) {
		Collection<LhsRhsPair> results = LevelRetriever.create(levelFunction, level).get(root);
		return StreamUtils.seq(results)
			.map(this::asGrouped)
			.toList();
	}

	@Timed
	@Override
	public double[] getMaxThresholds(MDSite lhs, int[] rhsAttrs) {
		return root.getMaxThreshold(lhs, rhsAttrs, 0);
	}

	@Override
	public int size() {
		return levelFunction.size();
	}

	private LatticeMD asGrouped(LhsRhsPair pair) {
		return LatticeMDImpl.builder()
			.lattice(this)
			.lhs(pair.getLhs())
			.rhs(pair.getRhs())
			.build();
	}

	private int getDistance(MD md) {
		MDSite lhs = md.getLhs();
		return levelFunction.getDistance(lhs);
	}

	private void updateMaxLevel(MD md) {
		int distance = getDistance(md);
		depth = Math.max(distance, depth);
	}
}
