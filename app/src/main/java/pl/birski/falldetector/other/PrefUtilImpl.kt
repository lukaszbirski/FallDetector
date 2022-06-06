package pl.birski.falldetector.other

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import javax.inject.Inject
import pl.birski.falldetector.R
import pl.birski.falldetector.service.enum.Algorithms

class PrefUtilImpl @Inject constructor(
    private val context: Context
) : PrefUtil {

    private var sp: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    override fun getTimerLength() = sp.getString(
        context.getString(R.string.shared_preferences_timer_length_list_key),
        "2"
    )?.toLong() ?: 2L

    override fun getDetectionAlgorithm(): Algorithms {
        val selectedValue = sp.getString(
            context.getString(R.string.shared_preferences_detection_algorithms_list_key),
            "1"
        )
        return selectedValue?.let { Algorithms.getByValue(it) } ?: Algorithms.FIRST
    }

    override fun isSendingMessageAllowed() = sp.getBoolean(
        context.getString(R.string.shared_preferences_send_message_key),
        false
    )
}
