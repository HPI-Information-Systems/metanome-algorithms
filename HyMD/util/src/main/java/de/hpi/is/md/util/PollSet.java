package de.hpi.is.md.util;

import java.util.HashSet;

public class PollSet<T> extends AbstractPollCollection<T> {

	public PollSet() {
		super(new HashSet<>());
	}
}
