package de.hpi.is.md.relational;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public abstract class AbstractRow implements Row {

	@NonNull
	private final Schema schema;
}
