package de.metanome.algorithms.cfdfinder.pattern;

import com.google.common.base.Joiner;
import de.metanome.algorithms.cfdfinder.CFDFinder;
import it.unimi.dsi.fastutil.ints.Int2ReferenceArrayMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Pattern implements Comparable<Pattern> {

    private Int2ReferenceArrayMap<PatternEntry> attributes;
    private int[] ids;

    private PatternEntry[] patternEntries;
    private float support;
    private List<IntArrayList> cover = new ArrayList<>();
    private int numKeepers;

    private static int[] primes = new int[] {2, 19, 29, 41, 59, 79, 101, 129, 151, 173, 197};

    public Pattern(Map<Integer, PatternEntry> attributes) {
        setAttributes(attributes);
    }

    public boolean matches(int[] tuple) {
        for (int i = 0; i < ids.length; i += 1) {
            int id = ids[i];
            PatternEntry entry = patternEntries[i];
            if (! entry.matches(tuple[id])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "(" + Joiner.on(",").join(attributes.values()) + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pattern pattern = (Pattern) o;

        if (attributes.size() != pattern.getAttributes().size()) {
            return false;
        }
        for (int i = 0; i < this.ids.length; i += 1) {
            if (this.ids[i] != pattern.ids[i]) {
                return false;
            }
        }
        for (int i = 0; i < this.patternEntries.length; i += 1) {
            if (! this.patternEntries[i].equals(pattern.patternEntries[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = 0;
        for (int i = 0; i < ids.length; i += 1) {
            int id = ids[i];
            PatternEntry entry = patternEntries[i];
            hashCode += id + primes[i % primes.length] * entry.hashCode();
        }
        return hashCode;
    }

    public Int2ReferenceArrayMap<PatternEntry> getAttributes() {
        return this.attributes;
    }

    public void setAttributes(Map<Integer, PatternEntry> attributes) {
        this.attributes = new Int2ReferenceArrayMap<>(attributes.size());
        this.patternEntries = new PatternEntry[attributes.size()];
        this.ids = new int[attributes.size()];

        int index = 0;
        for (Map.Entry<Integer, PatternEntry> entry : attributes.entrySet()) {
            this.attributes.put(entry.getKey().intValue(), entry.getValue());
            this.ids[index] = entry.getKey().intValue();
            this.patternEntries[index] = entry.getValue();
            index += 1;
        }
    }

    public int[] getIds() {
        return ids;
    }

    public PatternEntry[] getPatternEntries() {
        return patternEntries;
    }

    public float getSupport() {
        return support;
    }

    public void setSupport(float support) {
        this.support = support;
    }

    public float getConfidence() {
        return getNumCover() == 0 ? 0 : (float) getNumKeepers() / getNumCover();
    }

    public List<IntArrayList> getCover() {
        return cover;
    }

    public void setCover(List<IntArrayList> cover) {
        this.cover = cover;
    }

    public int getNumKeepers() {
        return numKeepers;
    }

    public void setNumKeepers(int numKeepers) {
        this.numKeepers = numKeepers;
    }

    public int getNumCover() {
        int tuples = 0;
        for (IntArrayList cluster : this.cover) {
            tuples += cluster.size();
        }
        return tuples;
    }

    public void updateCover(Pattern pattern) {
        IntSet tuples = new IntArraySet();
        for (IntArrayList cluster : pattern.getCover()) {
            tuples.addAll(cluster);
        }

        LinkedList<IntArrayList> clustersToRemove = new LinkedList<>();
        for (IntArrayList pc : this.getCover()) {
            pc.removeAll(tuples);
            if (pc.isEmpty()) {
                clustersToRemove.add(pc);
            }
        }
        this.getCover().removeAll(clustersToRemove);

        int globalCount = 0;
        for (IntArrayList cluster : this.getCover()) {
            globalCount += cluster.size();
        }
        this.setSupport(globalCount);
    }

    public void updateKeepers(int[] rhs) {
        int childViolations = 0;
        for (IntArrayList cluster : this.getCover()) {
            childViolations += CFDFinder.findViolationsFor(cluster, rhs);
        }
        this.setNumKeepers(this.getNumCover() - childViolations);
    }

    public double calculateG1(int[] rhs) {
        int violations = 0;
        for (IntArrayList cluster : this.getCover()) {
            for (int i = 0; i < cluster.size(); i += 1) {
                int iValue = rhs[cluster.getInt(i)];
                for (int j = 0; j < cluster.size(); j += 1) {
                    int jValue = rhs[cluster.getInt(j)];
                    if (iValue != jValue || iValue == -1) {
                        violations += 1;
                    }
                }
            }
        }
        return violations;
    }

    @Override
    public int compareTo(Pattern other) {
        int supportMeasure = (int) (other.support - this.support);
        if (supportMeasure == 0) {
            return other.numKeepers - this.numKeepers;
        }
        return supportMeasure;
    }
}
