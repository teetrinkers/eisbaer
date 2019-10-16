package de.theess.eisbaer.data

import io.requery.sql.KotlinEntityDataStore
import timber.log.Timber

class EntityStoreHolder(store: KotlinEntityDataStore<Any>?) {
    @Volatile
    var store = store.also { Timber.d("store holder changed: $it") }
}