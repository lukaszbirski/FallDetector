package pl.birski.falldetector.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import pl.birski.falldetector.components.implementations.LocationTrackerImpl
import pl.birski.falldetector.components.implementations.MessageSenderImpl
import pl.birski.falldetector.components.interfaces.LocationTracker
import pl.birski.falldetector.components.interfaces.MessageSender
import pl.birski.falldetector.other.PrefUtil
import pl.birski.falldetector.other.PrefUtilImpl
import pl.birski.falldetector.presentation.BaseApplication

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideApplication(@ApplicationContext app: Context): BaseApplication {
        return app as BaseApplication
    }

    @Singleton
    @Provides
    fun providePrefUtil(@ApplicationContext app: Context): PrefUtil = PrefUtilImpl(app)

    @Singleton
    @Provides
    fun provideMessageSender(
        @ApplicationContext app: Context,
        locationTracker: LocationTracker
    ): MessageSender =
        MessageSenderImpl(app, locationTracker)

    @Singleton
    @Provides
    fun provideLocationTracker(@ApplicationContext app: Context): LocationTracker =
        LocationTrackerImpl(app)
}
