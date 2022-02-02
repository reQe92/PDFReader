package com.drvidal.pdfreader.repository

import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase

class AnalyticsRepository {

    fun logException(throwable: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(throwable)
    }

    fun logReadedFile() {
        Firebase.analytics.logEvent("readed_file", null)
    }

    fun logShareApp() {
        Firebase.analytics.logEvent("share_app", null)
    }

    fun logShareFile() {
        Firebase.analytics.logEvent("share_file", null)
    }

}