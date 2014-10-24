package de.metanome.algorithms.depminer.depminer_helper.modules;

import de.metanome.algorithms.depminer.depminer_helper.modules.container.AgreeSet;

import java.util.*;

public class TupleEquivalenceClassRelation {

    List<RelationshipPair> relationships = new LinkedList<RelationshipPair>();

    public void addNewRelationship(int attributeID, int equivalenceClassID) {

        this.addNewRelationship(new RelationshipPair(attributeID, equivalenceClassID));
    }

    public void addNewRelationship(RelationshipPair pair) {

        this.relationships.add(pair);
    }

    public List<RelationshipPair> getRelationships() {

        return this.relationships;
    }

    public void mergeRelationshipsFrom(TupleEquivalenceClassRelation otherRelations) {

        this.relationships.addAll(otherRelations.getRelationships());
    }

    public void intersectWithAndAddToAgreeSetConcurrent(TupleEquivalenceClassRelation other, Map<AgreeSet, Object> agreeSets) {

        AgreeSet set = new AgreeSet();
        boolean intersected = false;
        for (RelationshipPair pair : this.relationships) {
            if (other.getRelationships().contains(pair)) {
                intersected = true;
                set.add(pair.getAttribute());
            }
        }

        if (intersected)
            agreeSets.put(set, new Object());
    }

    public void intersectWithAndAddToAgreeSet(TupleEquivalenceClassRelation other, Set<AgreeSet> agreeSets) {

        AgreeSet set = new AgreeSet();
        boolean intersected = false;
        for (RelationshipPair pair : this.relationships) {
            if (other.getRelationships().contains(pair)) {
                intersected = true;
                set.add(pair.getAttribute());
            }
        }

        if (intersected)
            agreeSets.add(set);
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + ((relationships == null) ? 0 : relationships.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TupleEquivalenceClassRelation other = (TupleEquivalenceClassRelation) obj;
        if (relationships == null) {
            if (other.relationships != null)
                return false;
        } else if (!relationships.equals(other.relationships))
            return false;
        return true;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        for (RelationshipPair pair : this.relationships) {
            builder.append(pair.toString()).append(", ");
        }
        return builder.substring(0, builder.length() - 2);
    }

    public static class RelationshipPair {

        private int[] relationship = new int[2];

        public RelationshipPair(int attribute, int ECIndex) {

            relationship[0] = attribute;
            relationship[1] = ECIndex;
        }

        public int getAttribute() {

            return this.relationship[0];
        }

        public int getIndex() {

            return this.relationship[1];
        }

        @Override
        public String toString() {

            return "(" + this.relationship[0] + ", " + this.relationship[1] + ")";
        }

        @Override
        public int hashCode() {

            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(relationship);
            return result;
        }

        @Override
        public boolean equals(Object obj) {

            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            RelationshipPair other = (RelationshipPair) obj;
            if (!Arrays.equals(relationship, other.relationship))
                return false;
            return true;
        }

    }
}
