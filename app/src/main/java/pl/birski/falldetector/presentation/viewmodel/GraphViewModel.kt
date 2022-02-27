package pl.birski.falldetector.presentation.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import pl.birski.falldetector.data.Accelerometer
import pl.birski.falldetector.model.Acceleration
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

    private val _data: MutableLiveData<Acceleration> = MutableLiveData()
    val data: LiveData<Acceleration> get() = _data

    init {
        _data.postValue(getValues())
    }

    fun startService() = sendCommandToService(ServiceActions.START_OR_RESUME)
        .also { accelerometer.initiateSensor(application) }

    fun stopService() = sendCommandToService(ServiceActions.STOP)
        .also {
            accelerometer.stopMeasurement()
            //startMeasurements()
        }

    private fun sendCommandToService(action: ServiceActions) =
        Intent(application, TrackingService::class.java).also {
            it.action = action.name
            application.startService(it)
        }

    private fun startMeasurements() {
        accelerometer.acceleration.value?.let {
            Timber.d("Measured value: ${accelerometer.acceleration.value}")
            _data.postValue(it)
        }
    }

    fun getValues() = accelerometer.acceleration.value
}
