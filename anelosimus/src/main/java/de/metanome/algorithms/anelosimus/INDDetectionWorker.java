package de.metanome.algorithms.anelosimus;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.ColumnPermutation;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.result_receiver.InclusionDependencyResultReceiver;
import de.metanome.algorithm_integration.results.InclusionDependency;
import de.metanome.algorithms.anelosimus.bitvectors.BitVector;

public class INDDetectionWorker implements Runnable {
    Logger logger = LoggerFactory.getLogger(INDDetectionWorker.class);

    ANELOSIMUS parent;
    int colStart, colEnd, id;
    InclusionDependencyResultReceiver receiver;

    Collection<InclusionDependency> uinds = new HashSet<>();
    long falsePositives = 0;
    long truePositives = 0;

    public INDDetectionWorker(ANELOSIMUS parent, int colStart, int colEnd, int id) {
        this.parent = parent;
        this.colStart = colStart;
        this.colEnd = colEnd;
        this.id = id;
        this.receiver = parent.resultReceiver;
    }

    private boolean checkContainment(int ref, int dep, boolean countFP) throws Exception {

        Set<String> refValueSet = this.parent.getValueSetFor(ref);

        Set<String> depValueSet = this.parent.getValueSetFor(dep);

        if (refValueSet.containsAll(depValueSet)) {
            return true;
        } else {
            if (countFP)
                this.falsePositives++;
            return false;
        }
    }

    private boolean filterRefLowCoverage(int ref, int dep) throws Exception {
        Set<String> refValueSet = this.parent.getValueSetFor(ref);

        Set<String> depValueSet = this.parent.getValueSetFor(dep);

        return (float) depValueSet.size() / refValueSet.size() * 100 < this.parent.refMinCoverage;
    }

    private boolean isEqual(int ref, int dep) throws Exception {

        Set<String> refValueSet = this.parent.getValueSetFor(ref);
        Set<String> depValueSet =
                this.parent.getValueSetFor(dep);
        return refValueSet.equals(depValueSet);

    }

    private void outputIND(int ref, int dep) {
        if (this.parent.outputINDS) {
            final String depTableName = this.parent.getTableNameFor(dep, this.parent.tableColumnStartIndexes);
            final String depColumnName = this.parent.columnNames.get(dep);

            final String refTableName = this.parent.getTableNameFor(ref, this.parent.tableColumnStartIndexes);
            final String refColumnName = this.parent.columnNames.get(ref);

            this.uinds.add(new InclusionDependency(new ColumnPermutation(new ColumnIdentifier(
                    depTableName, depColumnName)), new ColumnPermutation(new ColumnIdentifier(refTableName,
                    refColumnName))));
        }
        this.truePositives++;
    }

    private boolean isValid(int ref, int dep, boolean countFP) throws Exception {
        // skip trivial INDs
        if (ref == dep) {
            return false;
        }

        if (!this.parent.verify || this.checkContainment(ref, dep, countFP)) {
            if (this.parent.refMinCoverage > 0) {
                if (this.filterRefLowCoverage(ref, dep)) {
                    return false;
                }
            }

            return true;
        } else
            return false;
    }

    @Override
    public void run() {

        indLoop: for (int colId = this.colStart; colId < this.colEnd; colId++) {
            logger.trace("working on column {}", colId);
            if (!this.parent.isStrategyRef2Deps) {
                if (!this.parent.depCandidates.get(resolveColumn(colId))) {
                    continue indLoop;
                }

                if (this.parent.filterDependentRefs) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                final BitVector<?> refsForColumnI = this.parent.allOnes.copy();
                for (int j = 0; j < this.parent.bitMatrix.size(); j++) {
                    final BitVector<?> bJ = this.parent.bitMatrix.get(j);
                    if (bJ.get(colId)) {
                        refsForColumnI.and(bJ);
                    }
                }

                int lastRef = -1;
                while ((lastRef = refsForColumnI.next(lastRef)) > -1) {
                    try {
                        if (!this.parent.refCandidates.get(resolveColumn(lastRef))) {
                            continue;
                        }
                        if (isValid(resolveColumn(lastRef), resolveColumn(colId), true)) {
                            this.outputIND(resolveColumn(lastRef), resolveColumn(colId));
                        }

                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (this.parent.isStrategyRef2Deps) {
                if (!this.parent.refCandidates.get(resolveColumn(colId))) {
                    continue indLoop;
                }

                // if (!this.parent.dependentRefs.get(resolveColumn(colId))) {
                // continue indLoop;
                // }

                if (this.parent.filterDependentRefs) {
                    BitVector<?> refsForColumnI = this.parent.allOnes.copy();

                    for (int j = 0; j < this.parent.bitMatrix.size(); j++) {
                        final BitVector<?> bJ = this.parent.bitMatrix.get(j);
                        if (!bJ.get(colId)) {
                            // since the matrix is inverted for dep2ref
                            refsForColumnI.and(bJ.copy().flip());
                        }
                    }
                    if (refsForColumnI.count() > 1) {
                        if (this.parent.verify) {
                            int lastRef = -1;
                            while ((lastRef = refsForColumnI.next(lastRef)) > -1) {
                                try {
                                    if (this.isValid(resolveColumn(lastRef), resolveColumn(colId), false)) {
                                        if (!this.isEqual(resolveColumn(lastRef), resolveColumn(colId))) {
                                            if (this.parent.refCandidates.get(resolveColumn(lastRef))) {
                                                continue indLoop;
                                            }
                                        }
                                    }
                                } catch (final Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            continue indLoop;
                        }
                    }
                }

                BitVector<?> depsForColumnI = this.parent.allOnes.copy();

                for (int j = 0; j < this.parent.bitMatrix.size(); j++) {
                    final BitVector<?> bJ = this.parent.bitMatrix.get(j);
                    if (bJ.get(colId)) {
                        // no flipping, because we assume flipped matrix
                        depsForColumnI.and(bJ);
                    }
                }
                int lastDep = -1;
                while ((lastDep = depsForColumnI.next(lastDep)) > -1) {
                    try {
                        if (!this.parent.depCandidates.get(resolveColumn(lastDep))) {
                            continue;
                        }
                        if (this.isValid(resolveColumn(colId), resolveColumn(lastDep), true)) {
                            this.outputIND(resolveColumn(colId), resolveColumn(lastDep));
                            // if (this.parent.filterDependentRefs) {
                            // if (!this.isEqual(resolveColumn(colId), resolveColumn(lastDep))) {
                            // this.parent.dependentRefs.clear(resolveColumn(lastDep));
                            // }
                            // }
                        }

                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        }
        for (InclusionDependency ind : uinds) {
            try {
                this.receiver.receiveResult(ind);
            } catch (CouldNotReceiveResultException e) {
                e.printStackTrace();
            }
        }
        this.parent.numUnaryINDs.addAndGet(truePositives);
        this.parent.falsePositives.addAndGet(falsePositives);
        logger.debug("worker done (inds: {}, fp:{})", truePositives, falsePositives);
    }

    private int resolveColumn(int col) {
        if (this.parent.condenseMatrix) {
            return this.parent.condensedMatrixMapping[col];
        } else
            return col;
    }
}
