package de.theess.eisbaer.data

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import androidx.preference.PreferenceManager
import de.theess.eisbaer.BuildConfig
import de.theess.eisbaer.EisbaerApplication
import io.requery.android.sqlite.DatabaseSource
import io.requery.sql.KotlinEntityDataStore
import timber.log.Timber
import java.io.FileOutputStream

/**
 * Manages the requery data store.
 */
class Database(val context: Context) {

    /**
     * Open the database once to initialize the android_metadata table.
     * https://github.com/requery/requery/issues/856
     */
    internal class DbInitializer(context: Context, dbName: String) :
        SQLiteOpenHelper(context, dbName, null, 1) {
        override fun onCreate(db: SQLiteDatabase?) {}
        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {}

        fun initDatabase() {
            readableDatabase
            close()
        }
    }

    val entityStoreHolder: EntityStoreHolder = EntityStoreHolder(null)

    /**
     * Resets the holder if the database uri pref changes.
     */
    private val preferenceListener: SharedPreferences.OnSharedPreferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            if (key == EisbaerApplication.PREF_DATABASE_URI) synchronized(this) {
                Timber.d("database uri pref changed")
                initEntityStore()
            }
        }

    init {
        PreferenceManager.getDefaultSharedPreferences(context)
            .registerOnSharedPreferenceChangeListener(preferenceListener)
        initEntityStore()
    }

    /**
     * Gets and possibly initializes the store holder. If the holder is empty, the external
     * database is copied into the application data directory and then opened.
     */
    @Synchronized
    private fun initEntityStore() {
        val uri = getExternalDatabaseUri(context)
        if (uri == null || !checkUriGrant(context, uri)) {
            Timber.i("No database file")
            return
        }

        copyDatabase(context, uri)
        DbInitializer(context, DATABASE_NAME).initDatabase()

        val source = DatabaseSource(context, Models.DEFAULT, DATABASE_NAME, 1)
        if (BuildConfig.DEBUG) {
            source.setLoggingEnabled(true)
        }
        entityStoreHolder.store = KotlinEntityDataStore<Any>(source.configuration)
    }

    /**
     * Copies the database selected in the preferences to the internal database.
     */
    private fun copyDatabase(context: Context, externalDatabaseUri: Uri) {
        Timber.d("copy database from $externalDatabaseUri")

        // Internal database file.
        val dbPath = context.getDatabasePath(DATABASE_NAME)

        // Make sure we have a path to the file.
        dbPath?.parentFile?.mkdirs()

        // Copy data.
        FileOutputStream(dbPath).use {
            context.contentResolver.openInputStream(externalDatabaseUri)!!.copyTo(it)
        }

        Timber.d("copyDatabase done")
    }

    private fun checkUriGrant(context: Context, uri: Uri): Boolean {
        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            return true
        } catch (e: Exception) {
            Timber.d(e, "Failed to get uri grant.")
            return false
        }
    }

    private fun getExternalDatabaseUri(context: Context): Uri? {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(EisbaerApplication.PREF_DATABASE_URI, null)
            ?.let { Uri.parse(it) }
    }

    companion object {
        private const val DATABASE_NAME = "note_db"
    }
}