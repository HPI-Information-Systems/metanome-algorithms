package de.hpi.is.md.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.junit.Test;

@SuppressWarnings("ConstantConditions")
public class ReflectionUtilsTest {

	private static final String ANNOTATED = "annotated";
	private static final String NOT_ANNOTATED = "notAnnotated";
	private static final String ACCESSIBLE = "accessible";
	private static final String INACCESSIBLE = "inaccessible";
	private static final String PRIMITIVE_BOOLEAN = "primitiveBoolean";
	private static final String OBJECT_BOOLEAN = "objectBoolean";
	private static final String NOT_BOOLEAN = "notBoolean";

	@Test
	public void testGet() throws NoSuchFieldException {
		HelperClass obj = new HelperClass();
		Field field = HelperClass.class.getDeclaredField(ACCESSIBLE);
		boolean accessible = true;
		obj.setAccessible(accessible);
		assertThat(obj.getAccessible()).isEqualTo(accessible);
		assertThat(ReflectionUtils.get(field, obj)).hasValue(accessible);
	}

	@Test
	public void testGetAccessibleWithAccessible() throws NoSuchFieldException {
		HelperClass obj = new HelperClass();
		Field field = HelperClass.class.getDeclaredField(ACCESSIBLE);
		boolean accessible = true;
		obj.setAccessible(accessible);
		assertThat(obj.getAccessible()).isEqualTo(accessible);
		Optional<Boolean> value = ReflectionUtils.accessible(field)
			.apply(f -> ReflectionUtils.get(f, obj));
		assertThat(value).hasValue(accessible);
	}

	@Test
	public void testGetAnnotationIfPresent() throws NoSuchFieldException {
		Field annotated = HelperClass.class.getDeclaredField(ANNOTATED);
		Field notAnnotated = HelperClass.class.getDeclaredField(NOT_ANNOTATED);
		assertThat(ReflectionUtils.getAnnotationIfPresent(annotated, Deprecated.class)).isPresent();
		assertThat(ReflectionUtils.getAnnotationIfPresent(notAnnotated, Deprecated.class))
			.isEmpty();
	}

	@Test
	public void testGetDefaultValue() {
		assertThat(ReflectionUtils.getDefaultValue(HelperAnnotation.class, "property"))
			.hasValue("foo");
	}

	@Test
	public void testGetDefaultValueMissing() {
		assertThat(ReflectionUtils.getDefaultValue(HelperAnnotation.class, "missing")).isEmpty();
	}

	@Test
	public void testGetDefaultValueNoDefault() {
		assertThat(ReflectionUtils.getDefaultValue(HelperAnnotation.class, "noDefault")).isEmpty();
	}

	@Test
	public void testGetInaccessible()
		throws NoSuchFieldException {
		HelperClass obj = new HelperClass();
		Field field = HelperClass.class.getDeclaredField(INACCESSIBLE);
		boolean inaccessible = true;
		obj.setInaccessible(inaccessible);
		assertThat(obj.getInaccessible()).isEqualTo(inaccessible);
		assertThat(ReflectionUtils.get(field, obj)).isEmpty();
	}

	@Test
	public void testGetWithAccessible() throws NoSuchFieldException {
		HelperClass obj = new HelperClass();
		Field field = HelperClass.class.getDeclaredField(INACCESSIBLE);
		boolean inaccessible = true;
		obj.setInaccessible(inaccessible);
		assertThat(obj.getInaccessible()).isEqualTo(inaccessible);
		Optional<Boolean> value = ReflectionUtils.accessible(field)
			.apply(f -> ReflectionUtils.get(f, obj));
		assertThat(value).hasValue(inaccessible);
	}

	@Test
	public void testIsBooleanField() throws NoSuchFieldException {
		assertThat(
			ReflectionUtils.isBooleanField(HelperClass.class.getDeclaredField(PRIMITIVE_BOOLEAN)))
			.isTrue();
		assertThat(
			ReflectionUtils.isBooleanField(HelperClass.class.getDeclaredField(OBJECT_BOOLEAN)))
			.isTrue();
		assertThat(ReflectionUtils.isBooleanField(HelperClass.class.getDeclaredField(NOT_BOOLEAN)))
			.isFalse();
	}

	@Test
	public void testToggleAccessible() throws NoSuchFieldException {
		HelperClass obj = new HelperClass();
		Field field = HelperClass.class.getDeclaredField(ACCESSIBLE);
		boolean accessible = true;
		obj.setAccessible(accessible);
		field.setAccessible(true);
		assertThat(obj.getAccessible()).isEqualTo(accessible);
		ReflectionUtils.accessible(field)
			.apply(f -> ReflectionUtils.get(f, obj));
		assertThat(field.isAccessible()).isTrue();
	}

	@SuppressWarnings("all")
	private @interface HelperAnnotation {

		String noDefault();

		String property() default "foo";
	}

	@SuppressWarnings("all")
	private static class HelperClass {

		@Setter
		@Getter
		public Boolean accessible;
		public Boolean objectBoolean;
		public boolean primitiveBoolean;
		@Deprecated
		private Boolean annotated;
		private Boolean notAnnotated;
		@Setter
		@Getter
		private Boolean inaccessible;
		private Integer notBoolean;
	}
}
