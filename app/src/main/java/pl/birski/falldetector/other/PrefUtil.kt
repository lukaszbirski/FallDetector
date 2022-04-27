package pl.birski.falldetector.other

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import javax.inject.Inject
import pl.birski.falldetector.R
import pl.birski.falldetector.service.enum.Algorithms

class PrefUtil @Inject constructor(
    private val context: Context
) {

    private var sp: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun getTimerLength() = sp.getString(
        context.getString(R.string.shared_preferences_timer_length_list_key),
        "2"
    )?.toLong() ?: 2L

    fun getDetectionAlgorithm(): Algorithms {
        val selectedValue: String = sp.getString(
            context.getString(R.string.shared_preferences_detection_algorithms_list_key),
            "1"
        ) ?: "1"
        return Algorithms.getByValue(selectedValue) ?: Algorithms.IMPACT_POSTURE
    }

    fun isSendingMessageAllowed() = sp.getBoolean(
        context.getString(R.string.shared_preferences_send_message_key),
        false
    )
}
