package de.hpi.is.md.util.jackson;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Converters {

	public static <K, V> Map<K, V> toMap(Collection<Entry<K, V>> value) {
		ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();
		value.stream().map(Entry::toEntry).forEach(builder::put);
		return builder.build();
	}

	public static <K, V> Multimap<K, V> toMultimap(Collection<Entry<K, V>> value) {
		Builder<K, V> builder = ImmutableMultimap.builder();
		value.stream().map(Entry::toEntry).forEach(builder::put);
		return builder.build();
	}
}
