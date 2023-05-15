package fdiscovery.columns;

import java.awt.Point;
import java.util.Set;


import com.google.common.collect.Sets;

public class AgreeSet extends ColumnCollection {

	private static final long serialVersionUID = -5335032949377336772L;

	public AgreeSet(Set<Point> set1, Set<Point> set2, long numberOfColumns) {
		super(numberOfColumns);
		Set<Point> intersected = Sets.intersection(set1, set2);
		for (Point columnToIdentifier : intersected) {
			this.set(columnToIdentifier.x);
		}
	}
}
