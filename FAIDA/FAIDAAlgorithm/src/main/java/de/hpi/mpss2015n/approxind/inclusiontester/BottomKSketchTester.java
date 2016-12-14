package de.hpi.mpss2015n.approxind.inclusiontester;

import de.hpi.mpss2015n.approxind.datastructures.BottomKSketch;
import de.hpi.mpss2015n.approxind.utils.SimpleColumnCombination;

public final class BottomKSketchTester extends CombinedInclusionTester<BottomKSketch> {


    private final int sketchCapacityInBytes;

    public BottomKSketchTester(int sketchCapacityInBytes) {
        this.sketchCapacityInBytes = sketchCapacityInBytes;
    }

    @Override
    protected BottomKSketch createApproximateDatastructures(SimpleColumnCombination combination) {
        return new BottomKSketch(this.sketchCapacityInBytes);
    }

    @Override
    protected void insertRowIntoAD(SimpleColumnCombination combination, long hash, BottomKSketch bottomKSketch) {
        bottomKSketch.add(hash);
    }

    @Override
    protected boolean testWithAds(BottomKSketch bottomKSketch1, BottomKSketch bottomKSketch2) {
        return bottomKSketch2.dominates(bottomKSketch1);
    }

}
