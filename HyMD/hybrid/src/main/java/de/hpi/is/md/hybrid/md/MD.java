package de.hpi.is.md.hybrid.md;

public interface MD {

	MDSite getLhs();

	MDElement getRhs();

	default boolean isInLhs(int attr) {
		return getLhs().isSet(attr);
	}

	default boolean isRhs(int attr) {
		return getRhs().getId() == attr;
	}

}
