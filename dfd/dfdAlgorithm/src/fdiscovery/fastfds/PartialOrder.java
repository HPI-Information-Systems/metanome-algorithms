package fdiscovery.fastfds;

import gnu.trove.map.hash.TIntIntHashMap;

import java.util.ArrayList;
import java.util.Collections;

import fdiscovery.columns.DifferenceSet;
import fdiscovery.columns.DifferenceSets;

public class PartialOrder extends ArrayList<CoverOrder> {

	private static final long serialVersionUID = -4312148937513750522L;

	public PartialOrder(DifferenceSets differenceSets) {
		TIntIntHashMap orderMap = new TIntIntHashMap();
		
		for (DifferenceSet differenceSet : differenceSets) {
			// increase the cover count for set columns
			long bitIndex = 0;
			while (bitIndex < differenceSet.getNumberOfColumns()) {
				long currentNextSetBit = differenceSet.nextSetBit(bitIndex);
				if (currentNextSetBit != -1) {
					bitIndex = currentNextSetBit + 1;
					orderMap.putIfAbsent((int) currentNextSetBit, 0);
					orderMap.increment((int) currentNextSetBit);
				} else {
					bitIndex = differenceSet.getNumberOfColumns();
				}
			}
		}
		
		for (Integer index : orderMap.keys()) {
			this.add(new CoverOrder(index, orderMap.get(index)));
		}
		
		Collections.sort(this, Collections.reverseOrder());

	}
	
	public PartialOrder(DifferenceSets differenceSets, long columnIndexToSkip) {
		TIntIntHashMap orderMap = new TIntIntHashMap();

		for (DifferenceSet differenceSet : differenceSets) {
			// increase the cover count for set columns
			long bitIndex = columnIndexToSkip;
			while (bitIndex < differenceSet.getNumberOfColumns()) {
				long currentNextSetBit = differenceSet.nextSetBit(bitIndex);
				if (currentNextSetBit != -1) {
					bitIndex = currentNextSetBit + 1;
					orderMap.putIfAbsent((int) currentNextSetBit, 0);
					orderMap.increment((int) currentNextSetBit);
				} else {
					bitIndex = differenceSet.getNumberOfColumns();
				}
			}
		}
		
		for (Integer index : orderMap.keys()) {
			this.add(new CoverOrder(index, orderMap.get(index)));
		}
		
		Collections.sort(this, Collections.reverseOrder());

	}
	
	public ArrayList<Integer> getOrderedColumns() {
		ArrayList<Integer> orderedColumns = new ArrayList<>();
		for (CoverOrder order : this) {
			orderedColumns.add(order.getColumnIndex());
		}
		
		return orderedColumns;
	}
}
