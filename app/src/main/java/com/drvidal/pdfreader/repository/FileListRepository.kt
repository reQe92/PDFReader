package com.drvidal.pdfreader.repository

import android.Manifest
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.net.toUri
import com.drvidal.pdfreader.data.FileUri
import com.drvidal.pdfreader.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils


class FileListRepository constructor(private val context: Context, private val analyticsRepository: AnalyticsRepository) {

    private val permissionToCheck =  if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
        Manifest.permission.MANAGE_EXTERNAL_STORAGE
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    fun getFileUriFromUri(uri: Uri) : FileUri? {
        try {
            context.contentResolver.query(uri,
                null,
                null,
                null,
                null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                cursor.moveToFirst()
                val name = cursor.getString(nameIndex)
                val size = cursor.getLong(sizeIndex)
                return FileUri(uri, name.substringBeforeLast("."), size, null, null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            analyticsRepository.logException(e)
        }
        return null
    }

    fun hasStoragePermission() : Boolean {
        return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(
                context,
                permissionToCheck
            ) == PermissionChecker.PERMISSION_GRANTED
        }
    }

    suspend fun getAllPDFFiles(): List<FileUri> {
        return try {
            withContext(Dispatchers.IO) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                    getAllFileWithMedia()
                } else {
                    val rootDirectory = Environment.getExternalStorageDirectory()

                    FileUtils.listFiles(
                        rootDirectory,
                        arrayOf(Constants.PDF_EXTENSION),
                        true
                    ).sortedByDescending { it.lastModified() }
                        .map {
                            FileUri(
                                it.toUri(),
                                it.nameWithoutExtension,
                                it.length(),
                                it.lastModified(),
                                it.parentFile?.name
                            )
                        }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            analyticsRepository.logException(e)
            emptyList()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getAllFileWithMedia() : List<FileUri> {
        try {


            val uris = mutableListOf<FileUri>()
            val cr = context.applicationContext.contentResolver
            val uri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)

            val selectionMimeType = MediaStore.Files.FileColumns.MIME_TYPE + "=?"
            val mimeType = "application/pdf"
            val selectionArgsPdf = arrayOf(mimeType)

            val projection: Array<String>? = null
            val sortOrder: String = MediaStore.Files.FileColumns.DATE_MODIFIED + " desc"

            val allPdfFiles: Cursor? =
                cr.query(uri, projection, selectionMimeType, selectionArgsPdf, sortOrder)

            allPdfFiles?.use { cursor ->

                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val modifiedCol: Int =
                    cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
                val titleCol: Int = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.TITLE)
                val relativePathCol: Int =
                    cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.RELATIVE_PATH)
                val sizeCol: Int = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)

                while (cursor.moveToNext()) {
                    val uri = Uri.withAppendedPath(
                        MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL),
                        cursor.getString(idColumn)
                    )
                    val title = cursor.getString(titleCol)
                    val relativePath = cursor.getString(relativePathCol)
                    val size = cursor.getLong(sizeCol)
                    val lastModified = cursor.getLong(modifiedCol) * 1000
                    uris.add(
                        FileUri(
                            uri = uri,
                            nameWithoutExtension = title,
                            sizeInBytes = size,
                            lastModified = lastModified,
                            relativePath = relativePath
                        )
                    )
                }
            }
            return uris
        } catch (e: Exception) {
            e.printStackTrace()
            analyticsRepository.logException(e)
            return listOf()
        }
    }
}