package fdiscovery.tane;


import fdiscovery.general.CollectionSet;

import java.util.Iterator;

import fdiscovery.columns.ColumnCollection;

public class AprioriGeneration<T extends ColumnCollection> {
	
	private CollectionSet<ColumnCollection> currentLevel;
	
	public AprioriGeneration(CollectionSet<ColumnCollection> currentLevel) {
		this.currentLevel = currentLevel;
	}

	public CollectionSet<CollectionSet<ColumnCollection>> prefixBlocks() {
	CollectionSet<CollectionSet<ColumnCollection>> prefixBlocks = new CollectionSet<>();
	
	while (!this.currentLevel.isEmpty()) {
		CollectionSet<ColumnCollection> prefixBlock = new CollectionSet<>();
		
		ColumnCollection x = this.currentLevel.first();
		
		// singleton column collections are prefix blocks by default
		if (this.currentLevel.size() == 1) {
			prefixBlock.add(x);
			this.currentLevel.remove(x);
		}
		
		Iterator<ColumnCollection> currentLevelIterator = this.currentLevel.iterator();
		while (currentLevelIterator.hasNext()) {
			ColumnCollection y = currentLevelIterator.next();
			if (haveCommonPrefixBlock(x, y)) {
				prefixBlock.add(y);
				currentLevelIterator.remove();
			} else {
				break;
			}
		}
		prefixBlocks.add(prefixBlock);
	}
	
	return prefixBlocks;
}
	
//	public CollectionSet<CollectionSet<ColumnCollection>> prefixBlocks() {
//		CollectionSet<CollectionSet<ColumnCollection>> prefixBlocks = new CollectionSet<>();
//		
//		while (!this.currentLevel.isEmpty()) {
//			System.out.println("not empty");
//			System.out.println(currentLevel);
//			CollectionSet<ColumnCollection> prefixBlock = new CollectionSet<>();
//			
//			ColumnCollection x = this.currentLevel.first();
//			
//			// singleton column collections are prefix blocks by default
//			if (this.currentLevel.size() == 1) {
//				prefixBlock.add(x);
//				System.out.println("size 1 " + prefixBlock);
//				this.currentLevel.remove(x);
//			}
//			
//			Iterator<ColumnCollection> currentLevelIterator = this.currentLevel.iterator();
//			while (currentLevelIterator.hasNext()) {
//				ColumnCollection y = currentLevelIterator.next();
//				if (haveCommonPrefixBlock(x, y)) {
//					System.out.println("x " + x + " and y " + y + " common");
//					prefixBlock.add(y);
//					currentLevelIterator.remove();
//				} else {
//					break;
//				}
//			}
//			prefixBlocks.add(prefixBlock);
//		}
//		
//		System.out.println("prefix blocks:\t" + prefixBlocks);
//		return prefixBlocks;
//	}
	
//	public static void main(String[] args) {
//		ColumnCollection x = new ColumnCollection(3);
//		ColumnCollection y = new ColumnCollection(3);
//		
//		x.set(0);
//		y.set(1);
//		
//		System.out.println(haveCommonPrefixBlock(x, y));
//	}
	
	public static boolean haveCommonPrefixBlock(ColumnCollection x, ColumnCollection y) {
		ColumnCollection xXorY = x.xorCopy(y);
//		System.out.println("x:\t" + x);
//		System.out.println("y:\t" + y);
//		System.out.println("z:\t" + xXorY);
		long pos = Math.min(x.getMostRightBit(), y.getMostRightBit());
		if (xXorY.nextSetBit(0) == pos || xXorY.nextSetBit(0) == -1) {
//			System.out.println("true");
//			System.out.println("---------------------------");
			return true;
		}
//		System.out.println("false");
//		System.out.println("---------------------------");
		return false;
	}
}
