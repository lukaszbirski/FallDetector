package pl.birski.falldetector.data

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationManager.GPS_PROVIDER
import android.location.LocationManager.NETWORK_PROVIDER
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import pl.birski.falldetector.R

class LocationTracker(private val mContext: Context) : Service(), LocationListener {

    private var longitude: Double = 0.0
    private var latitude: Double = 0.0
    lateinit var locationManager: LocationManager
    private var loc: Location? = null
    private var checkGPS = false
    private var checkNetwork = false
    private var canGetLocation = false

    private val location: Location?
        @SuppressLint("MissingPermission")
        get() {
            try {
                locationManager = mContext.getSystemService(LOCATION_SERVICE) as LocationManager

                checkGPS = locationManager.isProviderEnabled(GPS_PROVIDER)

                checkNetwork = locationManager.isProviderEnabled(NETWORK_PROVIDER)

                if (!checkGPS && !checkNetwork) {
                    Toast.makeText(
                        mContext,
                        mContext.getString(R.string.location_tracker_toast_text),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {

                    canGetLocation = true

                    checkGPS.takeIf { it }.let {
                        if (checkGPS) {
                            locationManager.requestLocationUpdates(
                                GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                                this
                            )
                            loc = locationManager.getLastKnownLocation(GPS_PROVIDER).also {
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
                        loc = locationManager.getLastKnownLocation(NETWORK_PROVIDER).also {
                            it?.let {
                                latitude = it.latitude
                                longitude = it.longitude
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return loc
        }

    fun getLongitude(): Double {
        loc?.let { longitude = it.longitude }
        return longitude
    }

    fun getLatitude(): Double {
        loc?.let { latitude = it.latitude }
        return latitude
    }

    fun canGetLocation(): Boolean {
        return canGetLocation
    }

    fun showSettingsAlert(context: Context?) {
        val alertDialog = AlertDialog.Builder(
            mContext
        )
        alertDialog.setTitle(mContext.getString(R.string.location_tracker_dialog_title_text))
        alertDialog.setMessage(mContext.getString(R.string.location_tracker_dialog_message_text))
        alertDialog.setPositiveButton(
            mContext.getString(R.string.location_tracker_dialog_yes_text)
        ) { _, _ ->
            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).let {
                mContext.startActivity(it)
            }
        }
        alertDialog.setNegativeButton(
            mContext.getString(R.string.location_tracker_dialog_no_text)
        ) { dialog, _ ->
            dialog.cancel()
        }
        alertDialog.show()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onLocationChanged(location: Location) {
        loc?.let {
            it.latitude = location.latitude
            it.longitude = location.longitude
        }
    }

    override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {}
    override fun onProviderEnabled(s: String) {}
    override fun onProviderDisabled(s: String) {}

    companion object {
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES = 10f
        private const val MIN_TIME_BW_UPDATES: Long = 500
    }

    init {
        location
    }
}
