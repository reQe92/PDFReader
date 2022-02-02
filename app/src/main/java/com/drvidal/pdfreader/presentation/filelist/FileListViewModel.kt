package com.drvidal.pdfreader.presentation.filelist

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drvidal.pdfreader.data.FileUri
import com.drvidal.pdfreader.repository.FileListRepository
import com.drvidal.pdfreader.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FileListViewModel @Inject constructor(private val fileListRepository: FileListRepository) : ViewModel() {

    private val _fileUris = MutableLiveData<Resource<List<FileUri>>>()
    val fileUris: LiveData<Resource<List<FileUri>>> = _fileUris

    fun getFileUriFromUri(uri: Uri) : FileUri? {
        return fileListRepository.getFileUriFromUri(uri)
    }

    fun hasStoragePermission() : Boolean {
        return fileListRepository.hasStoragePermission()
    }

    fun getAllPDFFiles() = viewModelScope.launch {
        _fileUris.postValue(Resource.loading(null))
        val allFiles = fileListRepository.getAllPDFFiles()
        _fileUris.postValue(Resource.success(allFiles))
    }

}