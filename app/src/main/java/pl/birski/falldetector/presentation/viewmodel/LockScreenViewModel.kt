package pl.birski.falldetector.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.birski.falldetector.R
import pl.birski.falldetector.data.MessageSender
import pl.birski.falldetector.other.PrefUtil
import pl.birski.falldetector.usecase.UseCaseFactory

@HiltViewModel
class LockScreenViewModel
@Inject
constructor(
    private val application: Application,
    private val prefUtil: PrefUtil,
    private val messageSender: MessageSender,
    private val useCaseFactory: UseCaseFactory
) : ViewModel() {

    private var timerLengthSeconds = calculateTimeFromPrefs()
    private var secondsRemaining = calculateTimeFromPrefs()

    private fun calculateTimeFromPrefs() = prefUtil.getTimerLength() * 60

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

    fun sendMessages() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = useCaseFactory.getAllContactsUseCase.execute()
            val array: Array<String> = result.map {
                application.getString(
                    R.string.template_phone_number,
                    it.prefix,
                    it.number
                )
            }.toTypedArray()
            messageSender.startSendMessages(array)
        }
    }
}
