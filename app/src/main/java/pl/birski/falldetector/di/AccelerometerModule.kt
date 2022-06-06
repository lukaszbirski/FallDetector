package pl.birski.falldetector.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import pl.birski.falldetector.components.implementations.FallDetectorImpl
import pl.birski.falldetector.components.implementations.FilterImpl
import pl.birski.falldetector.components.implementations.SensorImpl
import pl.birski.falldetector.components.implementations.StabilizerImpl
import pl.birski.falldetector.components.interfaces.FallDetector
import pl.birski.falldetector.components.interfaces.Filter
import pl.birski.falldetector.components.interfaces.Sensor
import pl.birski.falldetector.components.interfaces.Stabilizer
import pl.birski.falldetector.other.PrefUtil

@Module
@InstallIn(SingletonComponent::class)
object AccelerometerModule {

    @Singleton
    @Provides
    fun provideSensor(
        fallDetector: FallDetector,
        stabilizer: Stabilizer
    ): Sensor = SensorImpl(fallDetector, stabilizer)

    @Singleton
    @Provides
    fun provideStabilizer(): Stabilizer = StabilizerImpl()

    @Singleton
    @Provides
    fun provideFilter(): Filter = FilterImpl()

    @Singleton
    @Provides
    fun provideFallDetector(
        @ApplicationContext app: Context,
        filter: Filter,
        prefUtil: PrefUtil
    ): FallDetector = FallDetectorImpl(app, filter, prefUtil)
}
