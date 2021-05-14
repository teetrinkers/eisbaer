package de.theess.eisbaer.data

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import timber.log.Timber
import java.io.FileNotFoundException
import java.io.InputStream

/**
 * A file from the Storage Access Framework.
 */
class StorageFile(private val uri: Uri, private val contentResolver: ContentResolver) {

    private var size: Int = 0
    private var lastModified: Long = 0

    init {
        if (checkUriGrant()) {
            readMetadata()
        }
    }

    private fun readMetadata() {
        Timber.d("Reading metadata")

        val cursor = contentResolver.query(uri, null, null, null, null)

        cursor?.use {
            // moveToFirst() returns false if the cursor has 0 rows.
            if (!it.moveToFirst()) {
                return
            }

            size = it.getIntOrNull(it.getColumnIndex(OpenableColumns.SIZE)) ?: 0
            lastModified =
                it.getLongOrNull(it.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED))
                    ?: 0
        }

    }

    override fun toString(): String {
        val hash = hash()
        return "StorageFile($hash)"
    }

    /**
     * Returns a hash of the file based on the metadata.
     */
    fun hash(): String {
        return "size=$size, lastModified=$lastModified, uri=$uri"
    }

    /**
     * Check whether we still can access the database uri.
     */
    fun checkUriGrant(): Boolean {
        return try {
            contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            true
        } catch (e: Exception) {
            Timber.d(e, "Failed to get uri grant.")
            false
        }
    }

    fun openInputStream(): InputStream? {
        try {
            return contentResolver.openInputStream(uri)
        } catch (ex : FileNotFoundException) {
            Timber.e("Database cannot be opened: $ex")
            return null
        }
    }

    /**
     * Tell the content provider to refresh (e.g. download) the file. Works only on Android 8+.
     */
    fun refresh() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            contentResolver.refresh(uri, null, null)
        } else {
            Timber.i("Refresh not supported on API level %d.", Build.VERSION.SDK_INT)
        }
    }
}