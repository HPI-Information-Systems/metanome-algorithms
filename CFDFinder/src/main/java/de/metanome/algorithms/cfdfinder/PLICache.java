package de.metanome.algorithms.cfdfinder;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import de.metanome.algorithms.cfdfinder.structures.PositionListIndex;

public class PLICache {

    private long size = 0;
    private long limit = 0;
    private Map<BitSet, PositionListIndex> cache = new HashMap<>();

    public PLICache(long limit) {
        this.limit = limit;
    }

    public PositionListIndex get(BitSet columns) {
        return cache.get(columns);
    }

    public void put(BitSet columns, PositionListIndex pli) {
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
    public PositionListIndex get(BitSet columns) {
        return null;
    }

    @Override
    public void put(BitSet columns, PositionListIndex pli) {}
}
