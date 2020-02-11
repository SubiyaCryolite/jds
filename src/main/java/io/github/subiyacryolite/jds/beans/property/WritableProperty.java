package io.github.subiyacryolite.jds.beans.property;

public interface WritableProperty<T> {

    /**
     * Get the wrapped value.
     *
     * @return The current value
     */
    T getValue();

    /**
     * Set the wrapped value.
     *
     * @param value
     *            The new value
     */
    void setValue(T value);

}