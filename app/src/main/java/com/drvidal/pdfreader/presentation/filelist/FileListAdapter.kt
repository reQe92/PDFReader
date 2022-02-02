package com.drvidal.pdfreader.presentation.filelist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.drvidal.pdfreader.data.FileUri
import com.drvidal.pdfreader.databinding.ItemFileListBinding
import java.io.File


class FileListAdapter(
    private val onItemClicked: (FileUri) -> Unit,
    private val onLongItemClicked: (FileUri) -> Unit
) : ListAdapter<FileUri, FileViewHolder>(DIFF_CALLBACK)  {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = FileViewHolder(
        ItemFileListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ), parent.context
    )

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClicked, onLongItemClicked)
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<FileUri>() {
            override fun areItemsTheSame(oldItem: FileUri, newItem: FileUri): Boolean =
                oldItem.uri == newItem.uri

            override fun areContentsTheSame(oldItem: FileUri, newItem: FileUri): Boolean =
                oldItem == newItem
        }
    }

}
