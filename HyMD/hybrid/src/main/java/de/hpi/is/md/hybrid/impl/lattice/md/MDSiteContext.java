package de.hpi.is.md.hybrid.impl.lattice.md;

import de.hpi.is.md.hybrid.md.MDSite;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jooq.lambda.tuple.Tuple2;

@RequiredArgsConstructor
class MDSiteContext {

	@NonNull
	private final MDSite site;

	AttributeContext with(int[] rhsAttr) {
		return new AttributeContext(rhsAttr);
	}

	@RequiredArgsConstructor
	class AttributeContext {

		private final int[] attrs;

		<T> T disableAndDo(Supplier<T> action) {
			Collection<Tuple2<Integer, Double>> old = remove();
			T result = action.get();
			old.forEach(t -> set(t.v1(), t.v2()));
			return result;
		}

		private Tuple2<Integer, Double> getOld(int attr) {
			return new Tuple2<>(attr, site.getOrDefault(attr));
		}

		private Collection<Tuple2<Integer, Double>> remove() {
			Collection<Tuple2<Integer, Double>> old = Arrays.stream(attrs)
				.mapToObj(this::getOld)
				.collect(Collectors.toList());
			for (int attr : attrs) {
				site.clear(attr);
			}
			return old;
		}

		private void set(int attr, double threshold) {
			site.set(attr, threshold);
		}
	}
}
