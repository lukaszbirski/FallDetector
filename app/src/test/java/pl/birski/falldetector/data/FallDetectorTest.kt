package pl.birski.falldetector.data

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.Mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.internal.DoNotInstrument

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
@DoNotInstrument
class FallDetectorTest {

    // system in test
    private lateinit var fallDetector: FallDetectorImpl

    @Mock
    private var context: Context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun setup() {
    }
}
