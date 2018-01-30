package de.hpi.is.md.hybrid.impl.sampling;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Statistics {

	private int processed = 0;
	private int count = 0;
	private int recommendations = 0;
	private int newDeduced = 0;

	void add(Statistics statistics) {
		this.count += statistics.count;
		this.processed += statistics.processed;
		this.newDeduced += statistics.newDeduced;
		this.recommendations += statistics.recommendations;
	}

	void count() {
		count++;
	}

	void newDeduced() {
		newDeduced++;
	}

	void processed() {
		processed++;
	}
}
