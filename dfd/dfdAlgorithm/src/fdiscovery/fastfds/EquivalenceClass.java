package fdiscovery.fastfds;

import java.awt.Point;
import java.util.HashSet;
import java.util.Iterator;

public class EquivalenceClass extends HashSet<Point> {

	private static final long serialVersionUID = -1326656356702786656L;

	@Override
	public String toString() {
		StringBuilder outputBuilder = new StringBuilder();
		for (Iterator<Point> it = this.iterator(); it.hasNext(); ) {
			Point identifier = it.next();
			outputBuilder.append(String.format("(%s,%d),", (char)(identifier.x+65), identifier.y));
		}
		
		return outputBuilder.toString();
	}
}
