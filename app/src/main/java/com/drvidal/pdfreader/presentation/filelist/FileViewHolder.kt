package com.drvidal.pdfreader.presentation.filelist

import android.content.Context
import android.text.format.DateUtils
import android.text.format.Formatter
import androidx.recyclerview.widget.RecyclerView
import com.drvidal.pdfreader.data.FileUri
import com.drvidal.pdfreader.databinding.ItemFileListBinding

class FileViewHolder(private val binding: ItemFileListBinding, private val context: Context) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(
        fileUri: FileUri,
        onItemClicked: (FileUri) -> Unit,
        onLongItemClicked: (FileUri) -> Unit
    ) {
        val size = Formatter.formatFileSize(context, fileUri.sizeInBytes)
        var lastModified = ""
        fileUri.lastModified?.let {
            lastModified = DateUtils
                .getRelativeTimeSpanString(
                    it,
                    System.currentTimeMillis(),
                    DateUtils.SECOND_IN_MILLIS
                ).toString()
        }

        binding.textName.text = fileUri.nameWithoutExtension
        binding.textDate.text = lastModified
        binding.textRelativePath.text = fileUri.relativePath
        binding.textSize.text = size

        binding.root.setOnClickListener {
            onItemClicked(fileUri)
        }

        /* binding.root.setOnLongClickListener {
             onLongItemClicked(fileUri)
             true
         }*/
    }
}
