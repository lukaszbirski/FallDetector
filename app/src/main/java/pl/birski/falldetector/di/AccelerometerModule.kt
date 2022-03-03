package pl.birski.falldetector.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import pl.birski.falldetector.data.Normalizer

@Module
@InstallIn(SingletonComponent::class)
object AccelerometerModule {

    @Singleton
    @Provides
    fun provideNormalizer() = Normalizer()
}
