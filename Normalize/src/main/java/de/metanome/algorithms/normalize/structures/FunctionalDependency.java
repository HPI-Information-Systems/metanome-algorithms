package de.metanome.algorithms.normalize.structures;

import java.util.BitSet;

import de.metanome.algorithms.normalize.utils.Utils;
import de.uni_potsdam.hpi.utils.CollectionUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;

public class FunctionalDependency {

	private BitSet lhs;
	private BitSet rhs;
	private Schema schema;
	
	private float fdScore;
	private float keyScore;
	
	public BitSet getLhs() {
		return this.lhs;
	}

	public BitSet getRhs() {
		return this.rhs;
	}

	public Schema getSchema() {
		return this.schema;
	}

	public BitSet getAttributes() {
		BitSet attributes = (BitSet) this.lhs.clone();
		attributes.or(this.rhs);
		return attributes;
	}

	public FunctionalDependency(BitSet lhs, BitSet rhs, Schema schema) {
		this.lhs = lhs;
		this.rhs = rhs;
		this.schema = schema;
		this.updateScores();
	}

	public void updateScores() {
		this.fdScore = -1.0f;
		this.keyScore = -1.0f;
	}
	
	public FunctionalDependency restrictedCopy(BitSet allowedRhsAttributes) {
		BitSet rhs = (BitSet) this.rhs.clone();
		rhs.and(allowedRhsAttributes);
		return new FunctionalDependency(this.lhs, rhs, this.schema);
	}
	
	public FunctionalDependency merge(FunctionalDependency other) {
		if (!this.schema.equals(other.getSchema()))
			return null;
		BitSet lhs = (BitSet) this.lhs.clone();
		BitSet rhs = (BitSet) this.rhs.clone();
		lhs.or(other.getLhs());
		rhs.or(other.getRhs());
		rhs.andNot(lhs);
		return new FunctionalDependency(lhs, rhs, this.schema);
	}

	public void removeKeyAttributes() {
		if (this.schema.getPrimaryKey() != null)
			this.rhs.andNot(this.schema.getPrimaryKey().getLhs());
	}

	public boolean compliesTo(BitSet attributes) {
		if (Utils.andNotCount(this.lhs, attributes) != 0)
			return false;
		
		if (Utils.andNotCount(this.rhs, attributes) == this.rhs.cardinality())
			return false;
		
		return true;
	}

	public boolean isKey() {
		return this.determinesEntireSchema() && !this.containsNullValuesInLhs();
	}
	
	protected boolean determinesEntireSchema() {
		return this.lhs.cardinality() + this.rhs.cardinality() == this.schema.getNumAttributes();
	}
	
	public boolean containsNullValuesInLhs() {
		for (int lhsAttr = this.lhs.nextSetBit(0); lhsAttr >= 0; lhsAttr = this.lhs.nextSetBit(lhsAttr + 1))
			if (this.schema.getNullValueCountOf(lhsAttr) != 0)
				return true;
		return false;
	}
	
	protected int numAttributesLeft(BitSet attributes) {
		return attributes.nextSetBit(0);
	}
	
	protected int numAttributesBetween(BitSet attributes) {
		int lastAttribute = 0;
		int numAttributes = 0;
		for (int attribute = attributes.nextSetBit(0); attribute >= 0; attribute = attributes.nextSetBit(attribute + 1)) {
			lastAttribute = attribute;
			numAttributes++;
		}
		return lastAttribute - (numAttributes - 1) - this.numAttributesLeft(attributes);
	}
	
	public boolean violatesConstraint() {
		for (Schema refSchema : this.schema.getReferencedSchemata()) {
			BitSet primaryKey = refSchema.getPrimaryKey().getLhs();
			if ((Utils.intersectionCount(primaryKey, this.rhs) != 0) && // The foreign key attributes do not stay in the relation
				(Utils.andNotCount(primaryKey, this.getAttributes()) != 0)) // The foreign key attributes are not all moved to the new relation
				return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return this.lhs.hashCode() + this.rhs.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof FunctionalDependency))
			return false;
		FunctionalDependency other = (FunctionalDependency) obj;
		return this.getLhs().equals(other.getLhs()) && this.getRhs().equals(other.getRhs()) && this.getSchema().equals(other.getSchema());
	}

	@Override
	public String toString() {
		IntArrayList lhsAttributes = new IntArrayList(this.lhs.cardinality());
		for (int attribute = this.lhs.nextSetBit(0); attribute >= 0; attribute = this.lhs.nextSetBit(attribute + 1))
			lhsAttributes.add(attribute);
		IntArrayList rhsAttributes = new IntArrayList(this.rhs.cardinality());
		for (int attribute = this.rhs.nextSetBit(0); attribute >= 0; attribute = this.rhs.nextSetBit(attribute + 1))
			rhsAttributes.add(attribute);
		return "[" + CollectionUtils.concat(lhsAttributes, ",") + "] --> " + CollectionUtils.concat(rhsAttributes, ",") + "\t(" + 
					this.keyScore() + " | " + this.fdScore() + ")\t" +
					this.keyLengthScore() + "\t" +
					this.keyValueScore() + "\t" +
					this.keyPositionScore() + "\t" + 
					this.fdLengthScore() + "\t" +
					this.fdPositionScore() + "\t" +
					this.fdDensityScore();
	}
	
	@Override
	public FunctionalDependency clone() {
		return new FunctionalDependency((BitSet) this.lhs.clone(), (BitSet) this.rhs.clone(), this.schema);
	}
	
	public String toFdString() {
		IntArrayList lhsAttributes = new IntArrayList(this.lhs.cardinality());
		for (int attribute = this.lhs.nextSetBit(0); attribute >= 0; attribute = this.lhs.nextSetBit(attribute + 1))
			lhsAttributes.add(attribute);
		IntArrayList rhsAttributes = new IntArrayList(this.rhs.cardinality());
		for (int attribute = this.rhs.nextSetBit(0); attribute >= 0; attribute = this.rhs.nextSetBit(attribute + 1))
			rhsAttributes.add(attribute);
		return this.getFdScoreString() + "\t[" + CollectionUtils.concat(lhsAttributes, ",") + "] --> " + CollectionUtils.concat(rhsAttributes, ",");
	}
	
	public String toKeyString() {
		IntArrayList lhsAttributes = new IntArrayList(this.lhs.cardinality());
		for (int attribute = this.lhs.nextSetBit(0); attribute >= 0; attribute = this.lhs.nextSetBit(attribute + 1))
			lhsAttributes.add(attribute);
		return this.getKeyScoreString() + "\t[" + CollectionUtils.concat(lhsAttributes, ",") + "]";
	}

	public String getFdScoreString() {
		return String.format("%1.8f (len %1.8f, pos %1.8f, val %1.8f, den %1.8f)",
				Float.valueOf(this.fdScore()),
				Float.valueOf(this.fdLengthScore()),
				Float.valueOf(this.fdPositionScore()),
				Float.valueOf(this.keyValueScore()),
				Float.valueOf(this.fdDensityScore()));
	}
	
	public String getKeyScoreString() {
		return String.format("%1.8f (len %1.8f, pos %1.8f, val %1.8f)",
				Float.valueOf(this.keyScore()),
				Float.valueOf(this.keyLengthScore()),
				Float.valueOf(this.keyPositionScore()),
				Float.valueOf(this.keyValueScore()));
	}
	
	/** Scores the FD for being a key **/
	public float keyScore() {
		if (this.keyScore < 0) {
			this.keyScore = (this.keyLengthScore() + this.keyValueScore() + this.keyPositionScore()) / 3;
			this.keyScore = (this.containsNullValuesInLhs()) ? 0 : this.keyScore;
		}
		return this.keyScore;
	}
	
	protected float keyLengthScore() {
		int length = this.lhs.cardinality();
		return (length == 0) ? 0 : (1.0f / length);
//		return 1 - (float) length / (float) this.schemaNumAttributes;
	}
	
	protected float keyValueScore() {
		int length = this.lhsMaxValueLength();
		return (length == 0) ? 0 : (1.0f / Math.max(1, length - 7)); // TODO: "-7" because we want to give a score of 1 to all keys that are up to 8 chars long

		//		float valueScore = 1.0f;
//		float valueLength = this.lhsMaxValueLength() / 8.0f; // TODO: "/ 8.0f" because we want to give a score of 1 to all keys that are up to 8 chars long
//		while (valueLength > 1) {
//			valueScore = valueScore / 2;
//			valueLength = valueLength / 2;
//		}
//		return valueScore;

//		return (this.maxValueLength == 0) ? 0 : (1.0f / (float) this.maxValueLength);
		// TODO: Is the variance of the values also a good indicator, i.e., does having all values of same/similar length indicate a key?
	}
	
	protected int lhsMaxValueLength() {
		int maxValueLength = 0;
		for (int lhsAttr = this.lhs.nextSetBit(0); lhsAttr >= 0; lhsAttr = this.lhs.nextSetBit(lhsAttr + 1))
//			maxValueLength += this.schemaStatistics.getMaxValueLengthOf(lhsAttr); // Do not take the sum, because the combination of many short values is still better than one very long value
			maxValueLength = Math.max(maxValueLength, this.schema.getMaxValueLengthOf(lhsAttr));
		return maxValueLength;
	}

	protected float keyPositionScore() {
		return (this.leftScore(this.lhs) + this.coherenceScore(this.lhs)) / 2;
	}
	
	protected float leftScore(BitSet attributes) {
		int attributesLeft = this.numAttributesLeft(attributes);
		return 1.0f / (attributesLeft + 1);
//		return 1 - (float) this.nonKeyAttributesLeft / (float) this.numColumns;
	}
	
	protected float coherenceScore(BitSet attributes) {
		int attributesBetween = this.numAttributesBetween(attributes);
		return 1.0f / (attributesBetween + 1);
//		return 1 - (float) this.nonKeyAttributesBetween / (float) this.numColumns;
	}
	
	/** Scores the FD for being a key-foreign-key **/
	public float fdScore() {
		if (this.fdScore < 0) {
			this.fdScore = (this.fdLengthScore() + this.keyValueScore() + this.fdPositionScore() + this.fdDensityScore()) / 4;
			this.fdScore = (this.containsNullValuesInLhs()) ? 0 : this.fdScore;
		}
		return this.fdScore;
	}
	
	protected float fdLengthScore() {
		float rhsLength = this.rhs.cardinality();
		float lhsLengthScore = this.keyLengthScore();
		float rhsLengthScore = (this.schema.getNumAttributes() > 2) ? rhsLength / (this.schema.getNumAttributes() - 2) : 0;
//		float rhsLengthScore = (rhsLength == 0) ? 0 : (1 - 1.0f / (float) rhsLength);
		return (lhsLengthScore + rhsLengthScore) / 2;
	}
	
	protected float fdPositionScore() {
		float lhsPositionScore = this.coherenceScore(this.lhs);
		float rhsPositionScore = this.coherenceScore(this.rhs);
		return (lhsPositionScore + rhsPositionScore) / 2;
	}
	
	private float fdDensityScore() {
		return (this.densityScore(this.lhs) + this.densityScore(this.rhs)) / 2;
//		return (this.lhsDensityScore() + this.rhsDensityScore()) / 2;
	}
	
/*	private float lhsDensityScore() {
		float densityScore = 0;
		for (int attribute = this.lhs.nextSetBit(0); attribute >= 0; attribute = this.lhs.nextSetBit(attribute + 1))
			densityScore += this.schema.getBloomFilterOf(attribute).expectedFpp();
		return 1 - densityScore / (float) this.lhs.cardinality();
	}
	
	private float rhsDensityScore() {
		float densityScore = 0;
		for (int attribute = this.rhs.nextSetBit(0); attribute >= 0; attribute = this.rhs.nextSetBit(attribute + 1))
			densityScore += this.schema.getBloomFilterOf(attribute).expectedFpp();
		return densityScore / (float) this.rhs.cardinality();
	}
*/	
	private float densityScore(BitSet attributes) {
		float densityScore = 0;
		for (int attribute = attributes.nextSetBit(0); attribute >= 0; attribute = attributes.nextSetBit(attribute + 1))
			densityScore += this.schema.getBloomFilterOf(attribute).expectedFpp();
		return 1 - densityScore / attributes.cardinality();
	}
}
