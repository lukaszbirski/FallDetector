package pl.birski.falldetector.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals

class LocationTrackerTest {

    // system in test
    private lateinit var locationTracker: LocationTracker

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        locationTracker = LocationTrackerImpl(context)
    }

    @Test
    fun checkIfCanGetLongitude() {

        val longitude = locationTracker.getLongitude()

        assertEquals(0.0, longitude, 0.001)
    }

    @Test
    fun checkIfCanGetLatitude() {

        val latitude = locationTracker.getLatitude()

        assertEquals(0.0, latitude, 0.001)
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

        // for location 0.0; 0.0 should be no address
        assertEquals(null, address)

        // then function should return only coordinates
        assertEquals("0.0, 0.0", location)
    }

    @Test
    fun checkIfLocationTrackerIsEnabled() {

        val enabled = locationTracker.locationEnabled()

        assertEquals(true, enabled)
    }
}
