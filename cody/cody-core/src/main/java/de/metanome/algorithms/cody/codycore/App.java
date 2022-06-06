package de.metanome.algorithms.cody.codycore;

import de.metanome.algorithms.cody.codycore.runner.ApproximateRunner;
import de.metanome.algorithms.cody.codycore.runner.BaseRunner;
import de.metanome.algorithms.cody.codycore.runner.ExactRunner;
import com.beust.jcommander.JCommander;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App {

    public static void main(String[] args) {
        Configuration config = new Configuration();
        JCommander cliParser = JCommander.newBuilder()
                .addObject(config)
                .build();
        cliParser.parse(args);

        if (config.isHelp()) {
            cliParser.usage();
            System.exit(0);
        }

        BaseRunner runner;
        if (config.getMinSupport() == 1.0) {
            runner = new ExactRunner(config);
        } else if (config.getMinSupport() < 1.0 && config.getMinSupport() > 2.0 / 3.0) {
            runner = new ApproximateRunner(config);
        } else {
            throw new IllegalArgumentException("Cannot run with the set minimum support");
        }

        runner.run();
    }
}
