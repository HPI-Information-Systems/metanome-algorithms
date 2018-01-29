package de.hpi.is.md.result;

public interface ResultEmitter<T> {

	ResultEmitter<T> register(ResultListener<T> resultListener);

	void unregisterAll();
}
