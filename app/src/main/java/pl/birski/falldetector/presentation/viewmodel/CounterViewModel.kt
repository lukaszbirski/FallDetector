package pl.birski.falldetector.presentation.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import pl.birski.falldetector.other.PrefUtil

@HiltViewModel
class CounterViewModel
@Inject
constructor(
    private val prefUtil: PrefUtil
) : ViewModel() {

    private var timerLengthSeconds = calculateTimeFromPrefs()
    private var secondsRemaining = calculateTimeFromPrefs()

    private fun calculateTimeFromPrefs() = prefUtil.getTimerLength().toLong() * 60

    fun getTimerLengthSeconds() = timerLengthSeconds

    fun getSecondsRemaining() = secondsRemaining

    fun countProgress() = (timerLengthSeconds - secondsRemaining).toInt()

    fun updateSecondsRemaining(millisUntilFinished: Long) {
        secondsRemaining = millisUntilFinished / 1000
    }

    fun getTextForCountdownTextView(): String {
        val minutes = secondsRemaining / 60
        val seconds = secondsRemaining - minutes * 60
        val secondsString = seconds.toString()
        return "$minutes:${if (secondsString.length == 2) secondsString else "0 $secondsString"}"
    }
}
