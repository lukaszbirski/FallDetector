package pl.birski.falldetector.presentation.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import pl.birski.falldetector.data.Sensor
import javax.inject.Inject
import pl.birski.falldetector.service.TrackingService
import pl.birski.falldetector.service.enum.ServiceActions

@HiltViewModel
class MainViewModel
@Inject
constructor(
    private val application: Application
) : ViewModel() {

    @Inject
    lateinit var sensor: Sensor

    fun startService() = sendCommandToService(ServiceActions.START_OR_RESUME)
        .also {
            sensor.initiateSensor(application)
        }

    fun stopService() = sendCommandToService(ServiceActions.STOP).also {
        sensor.stopMeasurement()
    }

    private fun sendCommandToService(action: ServiceActions) =
        Intent(application, TrackingService::class.java).also {
            it.action = action.name
            application.startService(it)
        }
}
