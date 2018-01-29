package de.hpi.is.md.jcommander;

import static org.assertj.core.api.Assertions.assertThat;

import com.beust.jcommander.Parameter;
import java.io.PrintStream;
import org.apache.commons.io.output.NullOutputStream;
import org.junit.Test;

public class JCommanderRunnerTest {

	private static JCommanderRunner createRunner(Application app) {
		return JCommanderRunner.create(app)
			.out(nullPrintStream())
			.build();
	}

	private static PrintStream nullPrintStream() {
		return new PrintStream(new NullOutputStream());
	}

	@Test
	public void testRunWithHelp() {
		TestApplication app = new TestApplication();
		JCommanderRunner runner = createRunner(app);
		runner.run("--help");
		assertThat(app.ran).isFalse();
	}

	@Test
	public void testRunWithRequired() {
		TestApplication app = new TestApplication();
		JCommanderRunner runner = createRunner(app);
		runner.run("-r");
		assertThat(app.ran).isTrue();
	}

	@Test
	public void testRunWithoutRequired() {
		TestApplication app = new TestApplication();
		JCommanderRunner runner = createRunner(app);
		runner.run();
		assertThat(app.ran).isFalse();
	}

	@SuppressWarnings({"FieldMayBeFinal", "unused"})
	private static class TestApplication implements Application {

		@Parameter(names = "--help", help = true)
		private boolean help = false;
		@Parameter(names = "-r", required = true)
		private boolean required;
		private boolean ran = false;

		@Override
		public void run() {
			ran = true;
		}
	}

}
