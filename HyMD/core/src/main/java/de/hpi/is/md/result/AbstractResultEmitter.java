package de.hpi.is.md.result;

import java.util.ArrayList;
import java.util.Collection;

public abstract class AbstractResultEmitter<T> implements ResultEmitter<T> {

	private final Collection<ResultListener<T>> resultListeners = new ArrayList<>();

	@Override
	public ResultEmitter<T> register(ResultListener<T> resultListener) {
		resultListeners.add(resultListener);
		return this;
	}

	@Override
	public void unregisterAll() {
		resultListeners.clear();
	}

	protected void emitResult(T result) {
		resultListeners.forEach(listener -> listener.receiveResult(result));
	}

}
