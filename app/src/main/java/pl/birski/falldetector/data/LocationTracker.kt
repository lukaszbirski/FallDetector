package pl.birski.falldetector.data

import android.app.Activity

interface LocationTracker {

    fun getLongitude(): Double

    fun getLatitude(): Double

    fun getAddress(): String?

    fun getAddressOrLocation(): String

    fun locationEnabled(): Boolean

    fun showSettingsAlert(activity: Activity)
}
