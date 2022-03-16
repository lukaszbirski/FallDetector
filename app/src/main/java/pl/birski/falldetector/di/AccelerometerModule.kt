package pl.birski.falldetector.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import pl.birski.falldetector.data.Filter
import pl.birski.falldetector.data.Normalizer
import pl.birski.falldetector.data.Stabilizer

@Module
@InstallIn(SingletonComponent::class)
object AccelerometerModule {

    @Singleton
    @Provides
    fun provideNormalizer() = Normalizer()

    @Singleton
    @Provides
    fun provideStabilizer() = Stabilizer()

    @Singleton
    @Provides
    fun provideLowPassFilter() = Filter()
}
