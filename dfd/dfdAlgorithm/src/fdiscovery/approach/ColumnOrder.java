package fdiscovery.approach;

import java.util.ArrayList;
import java.util.Collections;

import fdiscovery.approach.equivalence.EquivalenceManagedFileBasedPartition;
import fdiscovery.approach.equivalence.EquivalenceManagedFileBasedPartitions;
import fdiscovery.columns.ColumnCollection;
import fdiscovery.partitions.FileBasedPartition;
import fdiscovery.partitions.FileBasedPartitions;

// sorts partitions from lowest to highest distinct count
public class ColumnOrder {

	private int[] order;
	
	public ColumnOrder(FileBasedPartitions fileBasedPartitions) {
		this.order = new int[fileBasedPartitions.size()];
		ArrayList<FileBasedPartition> partitions = new ArrayList<>();
		partitions.addAll(fileBasedPartitions);
		Collections.sort(partitions);
//		Collections.sort(partitions, Collections.reverseOrder());
		int orderIndex = 0;
		for (FileBasedPartition partition : partitions) {
			order[orderIndex++] = partition.getIndex();
		}
	}

	public ColumnOrder(EquivalenceManagedFileBasedPartitions fileBasedPartitions) {
		this.order = new int[fileBasedPartitions.size()];
		ArrayList<EquivalenceManagedFileBasedPartition> partitions = new ArrayList<>();
		partitions.addAll(fileBasedPartitions);
		Collections.sort(partitions);
		int orderIndex = 0;
		for (EquivalenceManagedFileBasedPartition partition : partitions) {
			order[orderIndex++] = partition.getIndex();
		}
	}
	
	public int[] getOrderHighDistinctCount(ColumnCollection columns) {
		int[] columnIndices = columns.getSetBits();
		int[] orderForColumns = new int[columnIndices.length];
		
		int currentOrderIndex = 0;
		for (int i = 0; i < this.order.length; i++) {
			if (columns.get(this.order[i])) {
				orderForColumns[currentOrderIndex++] = this.order[i];
			}
		}
		
		return orderForColumns;
	}
	
	public int[] getOrderLowDistinctCount(ColumnCollection columns) {
		int[] columnIndices = columns.getSetBits();
		int[] orderForColumns = new int[columnIndices.length];
		
		int currentOrderIndex = 0;
		for (int i = this.order.length - 1; i >= 0; i--) {
			if (columns.get(this.order[i])) {
				orderForColumns[currentOrderIndex++] = this.order[i];
			}
		}
		
		return orderForColumns;
	}
}
