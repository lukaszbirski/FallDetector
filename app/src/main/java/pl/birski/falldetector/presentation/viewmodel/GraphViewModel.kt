package pl.birski.falldetector.presentation.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import pl.birski.falldetector.service.TrackingService
import pl.birski.falldetector.service.enum.ServiceActions
import javax.inject.Inject

@HiltViewModel
class GraphViewModel
@Inject
constructor(
    private val application: Application
) : ViewModel(){

    fun startService() = sendCommandToService(ServiceActions.START_OR_RESUME)

    fun stopService() = sendCommandToService(ServiceActions.STOP)

    private fun sendCommandToService(action: ServiceActions) =
        Intent(application, TrackingService::class.java).also {
            it.action = action.name
            application.startService(it)
        }
}
