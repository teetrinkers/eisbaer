package de.theess.eisbaer.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import timber.log.Timber
import java.util.*

/**
 * Base class for repositories. Provides support for converting database results to LiveData
 * objects, and updating the LiveDatas when the database changes.
 */
open class AbstractRepository(database: Database) : DatabaseListener {
    init {
        database.addListener(this)
    }

    /**
     * Map of LiveData to value function.
     *
     * We collect all LiveDatas in this map. When the database changes, the livedatas will be
     * updated using the value function.
     */
    private val liveDataMap: MutableMap<MutableLiveData<Any>, () -> Any?> =
        WeakHashMap<MutableLiveData<Any>, () -> Any?>()

    /**
     * Creates a LiveData with the value provided by the function, and stores both LiveData and
     * function for later updates.
     */
    protected fun <T> toLiveData(valueProvider: () -> T?): LiveData<T> {
        val liveData = MutableLiveData<T>()

        // We know that the function generates values of the type expected by the MutableLiveData.
        // Unfortunately, I don't know how to specify that for the map, so we use a cast.
        // Another solution might be to add a type parameter to AbstractRepository and restrict each
        // subclass to a single result type.
        @Suppress("UNCHECKED_CAST")
        liveDataMap.put(liveData as MutableLiveData<Any>, valueProvider)

        valueProvider()?.let {
            liveData.postValue(it)
        }

        return liveData
    }

    override fun update() {
        Timber.d("Updating %d listeners.", liveDataMap.size)
        // Update each LiveData using the provider function.
        liveDataMap.forEach {
            Timber.d("Updating note listener. Observers: %b", it.key.hasObservers())
            it.key.postValue(it.value())
        }
    }
}