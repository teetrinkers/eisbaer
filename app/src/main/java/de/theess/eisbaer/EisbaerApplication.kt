package de.theess.eisbaer

import android.app.Application
import greenberg.moviedbshell.logging.DebuggingTree
import greenberg.moviedbshell.logging.NoLogTree
import timber.log.Timber

class EisbaerApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(DebuggingTree())
        } else {
            Timber.plant(NoLogTree())
        }
    }

    companion object {
        const val PREF_DATABASE_URI: String = "database_uri"
    }
}