package fdiscovery.columns;


public class DifferenceSet extends ColumnCollection {

	private static final long serialVersionUID = -5174627424398542681L;
	
	private long numberOfColumns;
	
	public DifferenceSet(AgreeSet agreeSet) {
		super(agreeSet.getNumberOfColumns());
		this.numberOfColumns = agreeSet.getNumberOfColumns();
		
		this.or(agreeSet);
		this.flip(0, this.numberOfColumns);
	}
}
