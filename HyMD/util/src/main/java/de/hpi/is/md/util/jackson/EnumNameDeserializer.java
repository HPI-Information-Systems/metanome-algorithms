package de.hpi.is.md.util.jackson;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.DeserializerFactory;
import java.io.IOException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EnumNameDeserializer<T extends Enum<T>> extends JsonDeserializer<T> implements
	ContextualDeserializer {

	private final JavaType type;

	//Jackson
	private EnumNameDeserializer() {
		this.type = null;
	}

	private static JsonDeserializer<?> createEnumDeserializer(DeserializationContext ctxt,
		JavaType type) throws JsonMappingException {
		BeanDescription beanDesc = ctxt.getConfig().introspect(type);
		DeserializerFactory factory = ctxt.getFactory();
		return factory.createEnumDeserializer(ctxt, type, beanDesc);
	}

	@Override
	public JsonDeserializer<?> createContextual(DeserializationContext ctxt,
		BeanProperty property) {
		JavaType contextualType = ctxt.getContextualType();
		checkArgument(contextualType.isEnumType());
		return createFromType(contextualType);
	}

	@Override
	public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		checkNotNull(type);
		JsonDeserializer<T> deserializer = createEnumDeserializer(ctxt);
		if (p.currentToken() == JsonToken.FIELD_NAME && p.getCurrentName().equals("name")) {
			p.nextToken();
		} else {
			throw new IllegalArgumentException("Expected name field");
		}
		T obj = deserializer.deserialize(p, ctxt);
		p.nextToken();
		p.finishToken();
		p.clearCurrentToken();
		return obj;
	}

	@SuppressWarnings("unchecked")
	private JsonDeserializer<T> createEnumDeserializer(DeserializationContext ctxt)
		throws JsonMappingException {
		return (JsonDeserializer<T>) createEnumDeserializer(ctxt, type);
	}

	private JsonDeserializer<T> createFromType(JavaType contextualType) {
		return new EnumNameDeserializer<>(contextualType);
	}
}
