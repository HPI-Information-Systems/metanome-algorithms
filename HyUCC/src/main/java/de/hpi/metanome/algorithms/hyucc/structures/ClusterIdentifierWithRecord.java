package de.hpi.metanome.algorithms.hyucc.structures;

public class ClusterIdentifierWithRecord extends ClusterIdentifier {

	private final int record;
	
	public ClusterIdentifierWithRecord(final int[] cluster, final int record) {
		super(cluster);
		this.record = record;
	}

	public int getRecord() {
		return this.record;
	}
	
}
