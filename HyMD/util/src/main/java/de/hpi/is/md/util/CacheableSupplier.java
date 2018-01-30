package de.hpi.is.md.util;

import com.google.common.hash.HashCode;
import java.util.function.Supplier;

public interface CacheableSupplier<T> extends Supplier<T> {

	/**
	 * HashCode to uniquely identify the object create by this supplier
	 *
	 * @return hash code
	 */
	HashCode hash();

}
