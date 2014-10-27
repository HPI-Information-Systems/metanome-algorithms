package de.metanome.algorithms.depminer.depminer_helper.modules.container;

public abstract class StorageSet {

    @Override
    public String toString() {

        return this.toString_();
    }

    protected abstract String toString_();
}
