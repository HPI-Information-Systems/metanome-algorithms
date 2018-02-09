package de.metanome.algorithms.cfdfinder.result;

import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.result_receiver.ConditionalFunctionalDependencyResultReceiver;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class DirectOutputResultStrategy extends ResultStrategy {

    public DirectOutputResultStrategy(ConditionalFunctionalDependencyResultReceiver resultReceiver, ObjectArrayList<ColumnIdentifier> columnIdentifiers) {
        super(resultReceiver, columnIdentifiers);
    }

    public static String getIdentifier() {
        return "DirectOutputStrategy";
    }

    @Override
    public void receiveResult(Result result) {
        super.sendToMetanome(result);
    }

}
