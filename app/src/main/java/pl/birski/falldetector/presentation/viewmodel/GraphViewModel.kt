package pl.birski.falldetector.presentation.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import pl.birski.falldetector.data.Accelerometer
import pl.birski.falldetector.service.TrackingService
import pl.birski.falldetector.service.enum.ServiceActions
import timber.log.Timber

@HiltViewModel
class GraphViewModel
@Inject
constructor(
    private val application: Application
) : ViewModel() {

    @Inject
    lateinit var accelerometer: Accelerometer

    fun startService() = sendCommandToService(ServiceActions.START_OR_RESUME)
        .also { accelerometer.initiateSensor(application) }

    fun stopService() = sendCommandToService(ServiceActions.STOP)
        .also { accelerometer.stopMeasurement() }

    private fun sendCommandToService(action: ServiceActions) =
        Intent(application, TrackingService::class.java).also {
            it.action = action.name
            application.startService(it)
        }

    fun startMeasurements() {
        Timber.d("Measured value: ${accelerometer.acceleration.value}")
    }
}
