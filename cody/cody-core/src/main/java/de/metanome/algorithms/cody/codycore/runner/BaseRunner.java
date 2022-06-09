package de.metanome.algorithms.cody.codycore.runner;

import de.metanome.algorithms.cody.codycore.Configuration;
import de.metanome.algorithms.cody.codycore.candidate.CheckedColumnCombination;
import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseRunner {

    protected final Configuration configuration;

    /**
     * Contains all maximal valid ColumnCombinations
     */
    @Getter protected List<CheckedColumnCombination> resultSet;

    public BaseRunner(@NonNull Configuration configuration) {
        this.configuration = configuration;
        this.resultSet = new ArrayList<>();
    }

    public abstract void run();
}
