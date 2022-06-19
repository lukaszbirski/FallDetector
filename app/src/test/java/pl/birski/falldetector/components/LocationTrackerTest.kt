package pl.birski.falldetector.components

import android.content.Context
import android.location.Location
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.runner.RunWith
import org.mockito.Mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.internal.DoNotInstrument
import pl.birski.falldetector.components.implementations.LocationTrackerImpl

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
@DoNotInstrument
class LocationTrackerTest {

    // system in test
    private lateinit var locationTracker: LocationTrackerImpl

    @Mock
    private var context: Context = ApplicationProvider.getApplicationContext<Context>()

    val fakeLocation = Location("").also { location ->
        location.latitude = 52.22824846991743
        location.longitude = 20.9854009598633
    }

    @Before
    fun setup() {
        locationTracker = LocationTrackerImpl(context).also {
            it.location = fakeLocation
        }
    }

    @Test
    fun `check if can get longitude`() {
        val longitude = locationTracker.getLongitude()
        assertEquals(20.9854009598633, longitude, 0.001)
    }

    @Test
    fun `check if can get latitude`() {
        val latitude = locationTracker.getLatitude()
        assertEquals(52.22824846991743, latitude, 0.001)
    }

    @Test
    fun `check if can get address`() {
        val address = locationTracker.getAddress()
        assertEquals(null, address)
    }

    @Test
    fun `check if can get location when address is null`() {

        val address = locationTracker.getAddress()
        val location = locationTracker.getAddressOrLocation()

        // for test location address should be null
        assertEquals(null, address)

        // then function should return only coordinates
        assertEquals("52.22824846991743, 20.9854009598633", location)
    }

    @Test
    fun `check if location tracker is enabled`() {
        val enabled = locationTracker.locationEnabled()
        assertEquals(true, enabled)
    }
}
