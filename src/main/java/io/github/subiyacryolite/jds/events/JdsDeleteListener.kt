package io.github.subiyacryolite.jds.events

/**
 * This listener can be invoked on delete events
 */
interface JdsDeleteListener {
    /**
     * Action to perform when deleting a {@link io.github.subiyacryolite.jds.JdsEntity JdsEntity}
     */
    fun onDelete(arguments: OnDeleteEventArguments)
}
