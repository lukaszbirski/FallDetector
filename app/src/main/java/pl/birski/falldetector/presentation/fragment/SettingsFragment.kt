package pl.birski.falldetector.presentation.fragment

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import pl.birski.falldetector.R
import pl.birski.falldetector.other.PrefUtil

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {

    @Inject
    lateinit var prefUtil: PrefUtil

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        preference.key.takeIf {
            it == getString(R.string.shared_preferences_control_gyroscope_key) &&
                !prefUtil.isGyroscopeEnabled()
        }?.let { createSnackbar() }
        return super.onPreferenceTreeClick(preference)
    }

    private fun createSnackbar() {
        Snackbar.make(
            requireView(),
            getString(R.string.settings_fragment_gyroscope_disabled_text),
            Snackbar.LENGTH_LONG
        ).also {
            it.duration = 5000
            it.show()
        }
    }
}
