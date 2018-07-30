package de.hpi.is.md.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReflectionUtils {

	public static <A extends AccessibleObject> MakeAccessible<A> accessible(A accessibleObject) {
		return new MakeAccessible<>(accessibleObject);
	}

	@SuppressWarnings("unchecked")
	public static <T> Optional<T> get(Field field, Object obj) {
		try {
			T value = (T) field.get(obj);
			return Optional.ofNullable(value);
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	public static <T extends Annotation> Optional<T> getAnnotationIfPresent(
		AnnotatedElement annotatedElement, Class<T> annotationClass) {
		if (annotatedElement.isAnnotationPresent(annotationClass)) {
			T annotation = annotatedElement.getAnnotation(annotationClass);
			return Optional.of(annotation);
		}
		return Optional.empty();
	}

	public static <T> Optional<T> getDefaultValue(Class<? extends Annotation> annotation,
		String property) {
		try {
			Object defaultValue = annotation.getDeclaredMethod(property).getDefaultValue();
			return Optional.ofNullable(defaultValue)
				.map(CastUtils::as);
		} catch (NoSuchMethodException e) {
			return Optional.empty();
		}
	}

	public static boolean isBooleanField(Field field) {
		Class<?> type = field.getType();
		return boolean.class.isAssignableFrom(type) || Boolean.class.isAssignableFrom(type);
	}

	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	public static final class MakeAccessible<A extends AccessibleObject> {

		@NonNull
		private final A accessibleObject;

		public <T> T apply(Function<A, T> function) {
			boolean wasInaccessible = start();
			T result = function.apply(accessibleObject);
			finish(wasInaccessible);
			return result;
		}

		private void finish(boolean wasInaccessible) {
			if (wasInaccessible) {
				accessibleObject.setAccessible(false);
			}
		}

		private boolean start() {
			boolean wasInaccessible = !accessibleObject.isAccessible();
			if (wasInaccessible) {
				accessibleObject.setAccessible(true);
			}
			return wasInaccessible;
		}
	}
}
