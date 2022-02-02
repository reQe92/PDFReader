package com.drvidal.pdfreader.presentation.pdfrender

import androidx.lifecycle.ViewModel
import com.drvidal.pdfreader.repository.AnalyticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PDFRenderViewModel @Inject constructor(private val analyticsRepository: AnalyticsRepository) : ViewModel() {

    fun logException(throwable: Throwable) {
        analyticsRepository.logException(throwable)
    }

    fun logReadedFile() {
       analyticsRepository.logReadedFile()
    }

    fun logShareFile() {
        analyticsRepository.logShareFile()
    }

}