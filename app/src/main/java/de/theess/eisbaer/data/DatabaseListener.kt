package de.theess.eisbaer.data

/**
 * Listen for database changes.
 */
interface DatabaseListener {
    /**
     * This method is called when the database was updated.
     */
    fun update()
}