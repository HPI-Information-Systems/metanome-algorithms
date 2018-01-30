package de.hpi.is.md.util.jackson;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import java.io.IOException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SingletonDeserializer<T extends Enum<T>> extends
	JsonDeserializer<T> implements ContextualDeserializer {

	private final JavaType type;

	protected SingletonDeserializer() {
		this.type = null;
	}

	@Override
	public JsonDeserializer<?> createContextual(DeserializationContext ctxt,
		BeanProperty property) {
		JavaType contextualType = ctxt.getContextualType();
		checkArgument(contextualType.isEnumType());
		return createFromType(contextualType);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		checkNotNull(type);
		Class<T> rawClass = (Class<T>) type.getRawClass();
		p.readValueAsTree();
		return Enum.valueOf(rawClass, "INSTANCE");
	}

	private JsonDeserializer<T> createFromType(JavaType contextualType) {
		return new SingletonDeserializer<>(contextualType);
	}
}
