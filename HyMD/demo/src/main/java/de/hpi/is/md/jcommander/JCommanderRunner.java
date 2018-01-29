package de.hpi.is.md.jcommander;

import static de.hpi.is.md.util.ReflectionUtils.accessible;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import de.hpi.is.md.util.ReflectionUtils;
import java.io.PrintStream;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
public class JCommanderRunner {

	@NonNull
	private final Application app;
	@NonNull
	private final JCommander jc;
	@NonNull
	private final PrintStream out;

	public static JCommanderRunnerBuilder create(Application app) {
		return new JCommanderRunnerBuilder(app);
	}

	private static boolean isHelpField(AnnotatedElement annotatedElement) {
		return ReflectionUtils.getAnnotationIfPresent(annotatedElement, Parameter.class)
			.map(JCommanderRunner::isHelpParameter)
			.orElse(false);
	}

	private static boolean isHelpParameter(Parameter parameter) {
		return parameter.help();
	}

	public void run(String... args) {
		boolean success = parse(args);
		if (!success || needsHelp()) {
			printUsage();
			return;
		}
		app.run();
	}

	private Optional<Boolean> getBoolean(Field field) {
		return accessible(field).apply(this::getBooleanFromApp);
	}

	private Optional<Boolean> getBooleanFromApp(Field field) {
		return ReflectionUtils.get(field, app);
	}

	private boolean needsHelp() {
		Field[] fields = app.getClass().getDeclaredFields();
		return Stream.of(fields)
			.filter(ReflectionUtils::isBooleanField)
			.filter(JCommanderRunner::isHelpField)
			.findFirst()
			.flatMap(this::getBoolean)
			.orElse(false);
	}

	private boolean parse(String[] args) {
		try {
			jc.parse(args);
			return true;
		} catch (ParameterException e) {
			String message = e.getMessage();
			out.println(message);
		}
		return false;
	}

	private void printUsage() {
		StringBuilder usage = new StringBuilder();
		jc.usage(usage);
		out.println(usage);
	}

	@SuppressWarnings("UseOfSystemOutOrSystemErr")
	@Accessors(fluent = true)
	@Setter
	@RequiredArgsConstructor
	public static class JCommanderRunnerBuilder {

		@NonNull
		private final Application app;
		@NonNull
		private PrintStream out = System.out;

		public JCommanderRunner build() {
			JCommander jc = new JCommander(app);
			return new JCommanderRunner(app, jc, out);
		}
	}
}
