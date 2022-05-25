package pl.birski.falldetector.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals

class LocationTrackerTest {

    // system in test
    private lateinit var locationTracker: LocationTrackerImpl

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        locationTracker = LocationTrackerImpl(context).also {
            it.setLocationForTest()
        }
    }

    @Test
    fun checkIfCanGetLongitude() {
        val longitude = locationTracker.getLongitude()
        assertEquals(20.9854009598633, longitude, 0.001)
    }

    @Test
    fun checkIfCanGetLatitude() {
        val latitude = locationTracker.getLatitude()
        assertEquals(52.22824846991743, latitude, 0.001)
    }

    @Test
    fun checkIfCanGetAddress() {
        val address = locationTracker.getAddress()
        assertEquals(null, address)
    }

    @Test
    fun checkIfCanGetLocationWhenAddressIfNull() {

        val address = locationTracker.getAddress()
        val location = locationTracker.getAddressOrLocation()

        // for test location address should be null
        assertEquals(null, address)

        // then function should return only coordinates
        assertEquals("52.22824846991743, 20.9854009598633", location)
    }

    @Test
    fun checkIfLocationTrackerIsEnabled() {
        val enabled = locationTracker.locationEnabled()
        assertEquals(true, enabled)
    }
}
