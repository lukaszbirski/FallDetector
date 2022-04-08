package pl.birski.falldetector.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import pl.birski.falldetector.data.FallDetector
import pl.birski.falldetector.data.Filter
import pl.birski.falldetector.data.Normalizer
import pl.birski.falldetector.data.Sensor
import pl.birski.falldetector.data.Stabilizer
import pl.birski.falldetector.other.PrefUtil

@Module
@InstallIn(SingletonComponent::class)
object AccelerometerModule {

    @Singleton
    @Provides
    fun provideSensor(
        fallDetector: FallDetector,
        stabilizer: Stabilizer
    ) = Sensor(fallDetector, stabilizer)

    @Singleton
    @Provides
    fun provideNormalizer() = Normalizer()

    @Singleton
    @Provides
    fun provideStabilizer() = Stabilizer()

    @Singleton
    @Provides
    fun provideFilter() = Filter()

    @Singleton
    @Provides
    fun provideFallDetector(@ApplicationContext app: Context, filter: Filter, prefUtil: PrefUtil) =
        FallDetector(app, filter, prefUtil)
}
