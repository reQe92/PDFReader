package com.drvidal.pdfreader.presentation.settings

import androidx.lifecycle.ViewModel
import com.drvidal.pdfreader.repository.AnalyticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor
    (private val analyticsRepository: AnalyticsRepository) : ViewModel() {

    fun logShareApp() {
        analyticsRepository.logShareApp()
    }
}