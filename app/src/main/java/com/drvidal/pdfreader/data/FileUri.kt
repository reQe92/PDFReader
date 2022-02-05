package com.drvidal.pdfreader.data

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FileUri(val uri: Uri,
                   val nameWithoutExtension: String?,
                   val sizeInBytes: Long,
                   val lastModified: Long? = null,
                   val relativePath: String? = null,
                   var password: String? = null) : Parcelable