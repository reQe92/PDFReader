package com.drvidal.pdfreader.data

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
data class FileUri(val uri: Uri,
                   val nameWithoutExtension: String?,
                   val sizeInBytes: Long,
                   val lastModified: Long?,
                   val relativePath: String?) : Parcelable