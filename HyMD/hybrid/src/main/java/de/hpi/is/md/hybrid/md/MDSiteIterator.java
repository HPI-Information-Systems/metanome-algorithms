package de.hpi.is.md.hybrid.md;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class MDSiteIterator implements Iterator<MDElement> {

	@NonNull
	private final MDSite site;
	private int currentAttr = 0;

	@Override
	public boolean hasNext() {
		return nextElement().isPresent();
	}

	@Override
	public MDElement next() {
		return nextElement()
			.map(this::shift)
			.orElseThrow(NoSuchElementException::new);
	}

	private Optional<MDElement> nextElement() {
		return site.nextElement(currentAttr);
	}

	private MDElement shift(MDElement element) {
		currentAttr = element.getId() + 1;
		return element;
	}
}
