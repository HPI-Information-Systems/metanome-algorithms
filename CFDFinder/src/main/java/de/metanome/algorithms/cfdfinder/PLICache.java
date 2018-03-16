package de.metanome.algorithms.cfdfinder;

import de.metanome.algorithms.cfdfinder.structures.PositionListIndex;
import org.apache.lucene.util.OpenBitSet;

import java.util.HashMap;
import java.util.Map;

public class PLICache {

    private long size = 0;
    private long limit = 0;
    private Map<OpenBitSet, PositionListIndex> cache = new HashMap<>();

    public PLICache(long limit) {
        this.limit = limit;
    }

    public PositionListIndex get(OpenBitSet columns) {
        return cache.get(columns);
    }

    public void put(OpenBitSet columns, PositionListIndex pli) {
        if (size >= limit) {
            System.out.println("Limit reached");
            return;
        }
        cache.put(columns, pli);
        size += 1;
    }

}

class DisabledPLICache extends PLICache {

    public DisabledPLICache() {
        super(0);
    }

    @Override
    public PositionListIndex get(OpenBitSet columns) {
        return null;
    }

    @Override
    public void put(OpenBitSet columns, PositionListIndex pli) {}
}
