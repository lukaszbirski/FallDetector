package pl.birski.falldetector.presentation.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CounterViewModel
@Inject
constructor() : ViewModel() {

    private var timerLengthSeconds = 120L
    private var secondsRemaining = 120L

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
