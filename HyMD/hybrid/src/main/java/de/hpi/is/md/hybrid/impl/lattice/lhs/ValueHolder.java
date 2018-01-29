package de.hpi.is.md.hybrid.impl.lattice.lhs;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
class ValueHolder<T> {

	private T value;

}
