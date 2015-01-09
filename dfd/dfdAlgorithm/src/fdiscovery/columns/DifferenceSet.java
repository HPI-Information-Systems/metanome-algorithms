package fdiscovery.columns;


public class DifferenceSet extends ColumnCollection {

	private static final long serialVersionUID = -5174627424398542681L;
	
	private long numberOfColumns;
	
	public DifferenceSet(AgreeSet agreeSet) {
		super(agreeSet.getNumberOfColumns());
		this.numberOfColumns = agreeSet.getNumberOfColumns();
		
		this.bits = agreeSet.getBits().clone();
		this.flip(0, this.numberOfColumns);
	}
}
