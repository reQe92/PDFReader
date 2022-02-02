package com.drvidal.pdfreader.presentation.settings

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.drvidal.pdfreader.BuildConfig
import com.drvidal.pdfreader.R
import com.drvidal.pdfreader.util.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_screen, rootKey)

        findPreference<Preference>(Constants.PREFERENCE_VERSION)?.summary = getVersion()
        findPreference<Preference>(Constants.PREFERENCE_SHARE_APP)?.setOnPreferenceClickListener {
            shareApplication()
            true
        }
    }

    private fun shareApplication() {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        val appUrl = "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
        sendIntent.putExtra(
            Intent.EXTRA_TEXT,
            getString(R.string.checkout_app) + appUrl
        )
        sendIntent.type = "text/plain"
        startActivity(sendIntent)
        viewModel.logShareApp()
    }

    private fun getVersion(): String? {
        try {
            val pInfo = requireContext().packageManager.getPackageInfo(
                requireContext().packageName, 0
            )
            return pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return null
    }
}