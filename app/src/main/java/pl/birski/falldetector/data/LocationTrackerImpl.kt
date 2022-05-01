package pl.birski.falldetector.data

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationManager.GPS_PROVIDER
import android.location.LocationManager.NETWORK_PROVIDER
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
import android.widget.Toast
import java.util.Locale
import javax.inject.Inject
import pl.birski.falldetector.R

class LocationTrackerImpl @Inject constructor(
    private val context: Context
) : LocationTracker, Service(), LocationListener {

    private var longitude: Double = 0.0
    private var latitude: Double = 0.0
    private lateinit var locationManager: LocationManager
    private var locationNetwork: Location? = null
    private var locationGPS: Location? = null
    private var location: Location? = null
    private var checkGPS = false
    private var checkNetwork = false

    @SuppressLint("MissingPermission")
    private fun getLocation(): Location? {
        try {
            locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager

            checkGPS = locationManager.isProviderEnabled(GPS_PROVIDER)

            checkNetwork = locationManager.isProviderEnabled(NETWORK_PROVIDER)

            if (!checkGPS && !checkNetwork) {
                Toast.makeText(
                    context,
                    context.getString(R.string.location_tracker_toast_text),
                    Toast.LENGTH_SHORT
                ).show()
            } else {

                checkGPS.takeIf { it }.let {
                    if (checkGPS) {
                        locationManager.requestLocationUpdates(
                            GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            this
                        )
                        locationGPS = locationManager.getLastKnownLocation(GPS_PROVIDER).also {
                            it?.let {
                                latitude = it.latitude
                                longitude = it.longitude
                            }
                        }
                    }
                }

                checkNetwork.takeIf { it }.let {
                    locationManager.requestLocationUpdates(
                        NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES,
                        this
                    )
                    locationNetwork = locationManager.getLastKnownLocation(NETWORK_PROVIDER).also {
                        it?.let {
                            latitude = it.latitude
                            longitude = it.longitude
                        }
                    }
                }

                selectMoreAccurateLocation(
                    locationByGps = locationGPS,
                    locationByNetwork = locationNetwork
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return location
    }

    override fun getLongitude(): Double {
        location?.let { longitude = it.longitude }
        return longitude
    }

    override fun getLatitude(): Double {
        location?.let { latitude = it.latitude }
        return latitude
    }

    override fun locationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(GPS_PROVIDER)
    }

    private fun selectMoreAccurateLocation(locationByGps: Location?, locationByNetwork: Location?) {

        if (locationByGps != null && locationByNetwork != null) {
            if (locationByGps.accuracy > locationByNetwork.accuracy) {
                location?.let {
                    it.latitude = locationByGps.latitude
                    it.longitude = locationByGps.longitude
                }
            } else {
                location?.let {
                    it.latitude = locationByNetwork.latitude
                    it.longitude = locationByNetwork.longitude
                }
            }
        }
    }

    override fun showSettingsAlert(activity: Activity) {

        AlertDialog.Builder(activity).create().let {
            it.setTitle(activity.getString(R.string.location_tracker_dialog_title_text))
            it.setMessage(activity.getString(R.string.location_tracker_dialog_message_text))
            it.setButton(
                AlertDialog.BUTTON_POSITIVE,
                activity.getString(R.string.location_tracker_dialog_yes_text)
            ) { _, _ ->
                Intent(ACTION_LOCATION_SOURCE_SETTINGS).let { it ->
                    activity.startActivity(it)
                }
            }
            it.setButton(
                AlertDialog.BUTTON_NEGATIVE,
                activity.getString(R.string.location_tracker_dialog_no_text)
            ) { dialog, _ ->
                dialog.cancel()
            }
            it.show()
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onLocationChanged(location: Location) {

        when (location.provider) {

            NETWORK_PROVIDER -> locationNetwork?.let {
                it.latitude = location.latitude
                it.longitude = location.longitude
            }

            GPS_PROVIDER -> locationGPS?.let {
                it.latitude = location.latitude
                it.longitude = location.longitude
            }
        }

        selectMoreAccurateLocation(
            locationByGps = locationGPS,
            locationByNetwork = locationNetwork
        )
    }

    override fun getAddress(): String? {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses: List<Address> = geocoder.getFromLocation(latitude, longitude, 1)

        return addresses
            .takeIf { it.isNotEmpty() }
            ?.first()
            ?.getAddressLine(0)
            ?.substringBeforeLast(',')
    }

    override fun getAddressOrLocation(): String {
        return getAddress() ?: "${getLatitude()}, ${getLongitude()}"
    }

    override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {}
    override fun onProviderEnabled(s: String) {}
    override fun onProviderDisabled(s: String) {}

    companion object {
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES = 10f
        private const val MIN_TIME_BW_UPDATES: Long = 500
    }

    init {
        getLocation()
    }
}
