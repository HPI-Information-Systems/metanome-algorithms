package de.hpi.is.md.hybrid.impl.level;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Statistics {

	private int invalid = 0;
	private int notSupported = 0;
	private int newDeduced = 0;
	private int found = 0;
	private int validated = 0;
	private int groupedValidations = 0;
	private int rounds = 0;

	public void add(Statistics statistics) {
		this.invalid += statistics.invalid;
		this.notSupported += statistics.notSupported;
		this.newDeduced += statistics.newDeduced;
		this.found += statistics.found;
		this.validated += statistics.validated;
		this.groupedValidations += statistics.groupedValidations;
		this.rounds += statistics.rounds;
	}

	public void found() {
		found++;
	}

	public void groupedValidation() {
		groupedValidations++;
	}

	public void invalid() {
		invalid++;
	}

	public void newDeduced() {
		newDeduced++;
	}

	public void notSupported() {
		notSupported++;
	}

	public void round() {
		rounds++;
	}

	public void validated() {
		validated++;
	}
}
