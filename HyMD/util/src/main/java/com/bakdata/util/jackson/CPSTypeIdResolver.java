/*
 * Copyright (c) 2018 bakdata GmbH
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.bakdata.util.jackson;

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CPSTypeIdResolver implements TypeIdResolver {

	private static Map<Class<?>, BiMap<String, Class<?>>> GLOBAL_MAP;
	private static final Collection<ClassLoader> classLoaders = new ArrayList<>();

	private JavaType baseType;
	private BiMap<String, Class<?>> typeMap;

	public static Set<Class<?>> listImplementations(Class<?> base) {
		return GLOBAL_MAP.get(base).values();
	}

	private static BiMap<String, Class<?>> getTypeMap(Class<?> base) {
		if (GLOBAL_MAP == null) {
			scanClasspath();
		}
		return GLOBAL_MAP.getOrDefault(base, ImmutableBiMap.of());
	}

	public static void addClassLoader(ClassLoader classLoader) {
		classLoaders.add(classLoader);
	}

	private static void scanClasspath() {
		log.debug("Scanning Classpath");
		//scan classpaths for annotated child classes
		/*List<Class<?>> matches = CPScanner.scanClasses(new ClassFilter()
				.annotation(CPSType.class) //implicite OR
				.annotation(CPSBase.class));*/
		List<Class<?>> bases = new ArrayList<>();
		Set<Class<?>> types = new HashSet<>();
		FastClasspathScanner classpathScanner = new FastClasspathScanner();
		classLoaders.forEach(classpathScanner::addClassLoader);
		ScanResult scanRes = classpathScanner
			.matchClassesWithAnnotation(CPSTypes.class, types::add)
			.matchClassesWithAnnotation(CPSType.class, types::add)
			.matchClassesWithAnnotation(CPSBase.class, bases::add)
			.scan();

		log.debug("Scanned: {} classes in classpath", Integer.valueOf(scanRes.getNamesOfAllClasses().size()));

		GLOBAL_MAP = new HashMap<>();
		for (Class<?> type : types) {
			CPSType[] annos = type.getAnnotationsByType(CPSType.class);
			for (CPSType anno : annos) {
				BiMap<String, Class<?>> map = GLOBAL_MAP
					.computeIfAbsent(anno.base(), b -> HashBiMap.create());

				//check if base is marked as base
				CPSBase baseAnno = anno.base().getAnnotation(CPSBase.class);
				if (baseAnno == null) {
					throw new IllegalStateException(
						"The class " + anno.base() + " is used as a CPSBase in " + type
							+ " but not annotated as such.");
				}
				if (!anno.base().isAssignableFrom(type)) {
					throw new IllegalStateException(
						"The class " + anno.base() + " is used as a CPSBase in " + type
							+ " but type is no subclass of it.");
				}

				Class<?> oldEntry = map.put(anno.id(), type);

				if (oldEntry != null) {
					log.warn("\tmultiple classes with CPS id {} ({} and {})", anno.id(), type,
						oldEntry);
				}
			}
		}

		for (Class<?> b : bases) {
			log.debug("\tBase Class {}", b);
			BiMap<String, Class<?>> map = GLOBAL_MAP.get(b);
			if (map == null) {
				log.debug("\t\tNo registered types");
			} else {
				for (Entry<String, Class<?>> e : map.entrySet()) {
					log.debug("\t\t{}\t->\t{}", e.getKey(), e.getValue());
				}
			}

		}
	}

	@Override
	public void init(JavaType baseType) {
		this.baseType = baseType;
		this.typeMap = getTypeMap(baseType.getRawClass());
	}

	@Override
	public String idFromValue(Object value) {
		return idFromValueAndType(value, value.getClass());
	}

	@Override
	public String idFromValueAndType(Object value, Class<?> suggestedType) {
		String result = typeMap.inverse().get(suggestedType);
		if (result == null) {
			throw new IllegalStateException(
				"There is no id for the class " + suggestedType + " for " + baseType.getTypeName()
					+ ".");
		}
		return result;
	}

	@Override
	public String idFromBaseType() {
		return "DEFAULT";
	}

	@Override
	public JavaType typeFromId(DatabindContext context, String id) {
		Class<?> result = typeMap.get(id);
		if (result == null) {
			throw new IllegalStateException(
				"There is no type " + id + " for " + baseType.getTypeName() + ". Try: "
					+ getDescForKnownTypeIds());
		}
		return TypeFactory.defaultInstance().constructSpecializedType(baseType, result);
	}

	@Override
	public String getDescForKnownTypeIds() {
		return new TreeSet<>(typeMap.keySet()).toString();
	}

	@Override
	public Id getMechanism() {
		return Id.CUSTOM;
	}
}
