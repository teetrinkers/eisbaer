package de.theess.eisbaer.data

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.edit
import androidx.core.database.getIntOrNull
import androidx.preference.PreferenceManager
import de.theess.eisbaer.BuildConfig
import de.theess.eisbaer.EisbaerApplication
import de.theess.eisbaer.Models
import io.requery.android.sqlite.DatabaseSource
import io.requery.sql.KotlinEntityDataStore
import timber.log.Timber
import java.io.FileOutputStream

/**
 * Manages the requery data store.
 */
class Database(private val context: Context) {

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
     * Size of the external database. This value is persisted to the preferences.
     */
    private var dbFileSize: Int = 0
        set(value) {
            field = value
            PreferenceManager.getDefaultSharedPreferences(context).edit {
                putInt(EisbaerApplication.PREF_DATABASE_FILE_SIZE, value)
            }
        }

    /**
     * Resets the holder if the database uri pref changes.
     */
    private val preferenceListener: SharedPreferences.OnSharedPreferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == EisbaerApplication.PREF_DATABASE_URI) synchronized(this) {
                Timber.d("database uri pref changed")
                updateDatabase()
            }
        }

    init {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        preferences.registerOnSharedPreferenceChangeListener(preferenceListener)
        dbFileSize = preferences.getInt(EisbaerApplication.PREF_DATABASE_FILE_SIZE, 0)
        updateDatabase()
        if (!isOpen()) openDatabase()
    }

    /**
     * Gets and possibly initializes the store holder. If the holder is empty, the external
     * database is copied into the application data directory and then opened.
     */
    @Synchronized
    private fun updateDatabase() {
        val uri = getExternalDatabaseUri(context)
        if (uri == null || !checkUriGrant(context, uri)) {
            Timber.i("No database file uri.")
            return
        }

        if (context.getDatabasePath(DATABASE_NAME).exists()
            && !externalDatabaseChanged(context, uri)
        ) {
            Timber.d("Database has not changed.")
            return
        }

        Timber.d("Database changed.")

        copyDatabase(context, uri)
        DbInitializer(context, DATABASE_NAME).initDatabase()
        openDatabase()
    }

    /**
     * Opens the internal database.
     */
    private fun openDatabase() {
        val source = DatabaseSource(context, Models.DEFAULT, DATABASE_NAME, 1)
        if (BuildConfig.DEBUG) {
            source.setLoggingEnabled(true)
        }
        entityStoreHolder.store = KotlinEntityDataStore(source.configuration)
    }

    private fun isOpen(): Boolean {
        return entityStoreHolder.store != null
    }

    /**
     * Checks whether the external database file has changed.
     */
    private fun externalDatabaseChanged(context: Context, uri: Uri): Boolean {
        Timber.d("Checking database.")

        val cursor = context.contentResolver.query(uri, null, null, null, null)

        cursor?.use {
            // moveToFirst() returns false if the cursor has 0 rows.
            if (it.moveToFirst()) {
                val size: Int = it.getIntOrNull(it.getColumnIndex(OpenableColumns.SIZE)) ?: 0
                Timber.d("size: %d, previous size: %d", size, dbFileSize)
                val changed = size != dbFileSize
                dbFileSize = size
                return changed
            }
        }

        return true
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

    /**
     * Check whether we still can access the database uri.
     */
    private fun checkUriGrant(context: Context, uri: Uri): Boolean {
        return try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            true
        } catch (e: Exception) {
            Timber.d(e, "Failed to get uri grant.")
            false
        }
    }

    /**
     * Gets the URI to the external database file from the preferences.
     */
    private fun getExternalDatabaseUri(context: Context): Uri? {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(EisbaerApplication.PREF_DATABASE_URI, null)
            ?.let { Uri.parse(it) }
    }

    companion object {
        /**
         * Name of the internal database.
         */
        private const val DATABASE_NAME = "note_db"
    }
}