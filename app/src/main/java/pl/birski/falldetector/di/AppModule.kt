package pl.birski.falldetector.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import pl.birski.falldetector.data.MessageSender
import pl.birski.falldetector.data.MessageSenderImpl
import pl.birski.falldetector.other.PrefUtil
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
    fun providePrefUtil(@ApplicationContext app: Context) = PrefUtil(app)

    @Singleton
    @Provides
    fun provideMessageSender(@ApplicationContext app: Context): MessageSender =
        MessageSenderImpl(app)
}
