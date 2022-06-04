package pl.birski.falldetector.usecase

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import pl.birski.falldetector.database.AppDatabaseFake
import pl.birski.falldetector.database.ContactDaoFake
import pl.birski.falldetector.model.util.ContactMapper

class RemoveContactUseCaseTest {

    private val appDatabase = AppDatabaseFake()

    // system in test
    private lateinit var removeContactUseCase: RemoveContactUseCase

    // dependencies
    private lateinit var contactDao: ContactDaoFake
    private val mapper = ContactMapper()

    private lateinit var getAllContactsUseCase: GetAllContactsUseCase

    // want to remove first element from database
    private val contactToRemove = mapper.mapToDomainModel(appDatabase.contacts[0])

    @BeforeEach
    fun setup() {
        contactDao = ContactDaoFake(appDatabase)

        removeContactUseCase = RemoveContactUseCase(
            mapper = mapper,
            contactDao = contactDao
        )

        getAllContactsUseCase = GetAllContactsUseCase(
            mapper = mapper,
            contactDao = contactDao
        )
    }

    @Test
    fun `remove contact from database`(): Unit = runBlocking {

        removeContactUseCase.execute(contact = contactToRemove)

        val contacts = getAllContactsUseCase.execute().toList()

        // confirm that we removed element from database
        assert(contactToRemove !in contacts)
    }

    @AfterEach
    fun tearDown() { }
}
