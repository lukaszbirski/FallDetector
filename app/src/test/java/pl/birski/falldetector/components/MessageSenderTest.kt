package pl.birski.falldetector.components

import android.content.Context
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
import pl.birski.falldetector.R
import pl.birski.falldetector.components.fake.LocationTrackerFake
import pl.birski.falldetector.components.implementations.MessageSenderImpl
import pl.birski.falldetector.components.interfaces.LocationTracker
import pl.birski.falldetector.components.interfaces.MessageSender

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
@DoNotInstrument
class MessageSenderTest {

    // system in test
    private lateinit var messageSender: MessageSender

    private lateinit var locationTracker: LocationTracker

    @Mock
    private var context: Context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun setup() {
        locationTracker = LocationTrackerFake()
        messageSender = MessageSenderImpl(context, locationTracker)
    }

    @Test
    fun `check if sent message is correct`() {

        val message = context.getString(
            R.string.message_sender_text_body,
            locationTracker.getAddress(),
            locationTracker.getLongitude(),
            locationTracker.getLatitude()
        )

        val method = messageSender.javaClass.getDeclaredMethod(
            "getMessage"
        )

        method.isAccessible = true

        assertEquals(message, method.invoke(messageSender))
    }
}
