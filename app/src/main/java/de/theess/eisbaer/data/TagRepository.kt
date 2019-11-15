package de.theess.eisbaer.data

import androidx.lifecycle.LiveData
import de.theess.eisbaer.EisbaerApplication
import io.requery.kotlin.asc
import timber.log.Timber

class TagRepository(private val database: Database) : AbstractRepository(database) {

    fun getAll(): LiveData<List<Tag>> {
        Timber.d("getAll")
        return toLiveData {
            database.store
                ?.run {
                    select(Tag::class)
                        .orderBy(Tag::title.asc()).limit(QUERY_LIMIT)
                }?.get()?.toList()
        }
    }

    companion object {
        const val QUERY_LIMIT = 200

        // For Singleton instantiation
        @Volatile
        private var instance: TagRepository? = null

        fun getInstance(application: EisbaerApplication) =
            instance ?: synchronized(this) {
                instance ?: TagRepository(application.database).also {
                    instance = it
                }
            }
    }
}