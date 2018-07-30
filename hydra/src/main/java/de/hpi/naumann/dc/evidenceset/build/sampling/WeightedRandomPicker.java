package de.hpi.naumann.dc.evidenceset.build.sampling;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WeightedRandomPicker<T> {
	private int totalSum = 0;
	private List<Entry<T>> objects = new ArrayList<>();
	private Random r = new Random();

	private static class Entry<T> {
		private T object;
		private int weight;
		private int wSum;

		public Entry(T object, int weight, int wSum) {
			this.object = object;
			this.weight = weight;
			this.wSum = wSum;
		}
	}

	public void add(T add, int weight) {
		totalSum += weight;
		objects.add(new Entry<>(add, weight, totalSum));
	}

	public T getRandom() {
		return findChoice(r.nextInt(totalSum), 0, objects.size() - 1).object;
	}

	public T getRandom(int start, int end) {
		if (start == end - 1)
			return objects.get(start).object;

		int weightDiff = objects.get(end - 1).wSum - objects.get(start).wSum;
		int choice = r.nextInt(weightDiff) + objects.get(start).wSum;

		int lowGuess = start;
		int highGuess = end - 1;

		return findChoice(choice, lowGuess, highGuess).object;
	}

	public T getRandom(int without) {
		if (objects.size() == 1)
			return null;

		int weightDiff = totalSum - objects.get(without).weight;
		int choice = r.nextInt(weightDiff);
		if (choice >= objects.get(without).wSum - objects.get(without).weight)
			choice += objects.get(without).weight;

		int lowGuess = 0;
		int highGuess = objects.size() - 1;

		return findChoice(choice, lowGuess, highGuess).object;
	}

	private Entry<T> findChoice(int choice, int lowGuess, int highGuess) {
		while (highGuess > lowGuess) {
			int guess = (lowGuess + highGuess) / 2;
			Entry<T> guessEntry = objects.get(guess);
			if (choice >= guessEntry.wSum)
				lowGuess = guess + 1;
			else if (choice < guessEntry.wSum - guessEntry.weight)
				highGuess = guess - 1;
			else
				return guessEntry;
		}
		return objects.get(lowGuess);
	}
}
