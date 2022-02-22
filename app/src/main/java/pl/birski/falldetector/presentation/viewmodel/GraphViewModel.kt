package pl.birski.falldetector.presentation.viewmodel

import android.app.Application
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

    fun startService() = TrackingService.initiate(application, ServiceActions.START_OR_RESUME)

    fun stopService() = TrackingService.stop(application, ServiceActions.STOP)

}
