package pl.birski.falldetector.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import pl.birski.falldetector.database.AppDatabase
import pl.birski.falldetector.database.model.ContactEntity
import pl.birski.falldetector.model.Contact
import pl.birski.falldetector.model.util.ContactMapper
import pl.birski.falldetector.model.util.DomainMapper
import pl.birski.falldetector.usecase.AddContactUseCase
import pl.birski.falldetector.usecase.GetAllContactsUseCase
import pl.birski.falldetector.usecase.RemoveContactUseCase
import pl.birski.falldetector.usecase.UseCaseFactory

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {

    @ViewModelScoped
    @Provides
    fun provideUseCaseFactory(
        addDriverUseCase: AddContactUseCase,
        getAllContactsUseCase: GetAllContactsUseCase,
        removeContactUseCase: RemoveContactUseCase
    ): UseCaseFactory {
        return UseCaseFactory(
            addDriverUseCase = addDriverUseCase,
            getAllContactsUseCase = getAllContactsUseCase,
            removeContactUseCase = removeContactUseCase
        )
    }

    @ViewModelScoped
    @Provides
    fun provideContactMapper(): ContactMapper {
        return ContactMapper()
    }

    @ViewModelScoped
    @Provides
    fun provideAddContactUseCase(
        database: AppDatabase,
        mapper: ContactMapper
    ): AddContactUseCase {
        return AddContactUseCase(
            database = database,
            mapper = mapper
        )
    }

    @ViewModelScoped
    @Provides
    fun provideGetAllContactsUseCase(
        database: AppDatabase,
        mapper: ContactMapper
    ): GetAllContactsUseCase {
        return GetAllContactsUseCase(
            contactDao = database.contactDao(),
            mapper = mapper
        )
    }

    @ViewModelScoped
    @Provides
    fun provideRemoveContactUSeCase(
        database: AppDatabase,
        mapper: ContactMapper
    ): RemoveContactUseCase {
        return RemoveContactUseCase(
            contactDao = database.contactDao(),
            mapper = mapper
        )
    }
}
