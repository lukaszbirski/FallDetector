package pl.birski.falldetector.other

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import javax.inject.Inject
import pl.birski.falldetector.R

class PrefUtil @Inject constructor(
    private val context: Context
) {

    private var sp: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun getTimerLength(): String {
        return sp.getString(
            context.getString(R.string.shared_preferences_timer_length_list_key),
            "2"
        ) ?: "2"
    }
}
