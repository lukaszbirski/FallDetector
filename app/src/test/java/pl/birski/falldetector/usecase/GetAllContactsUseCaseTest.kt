package pl.birski.falldetector.usecase

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import pl.birski.falldetector.database.AppDatabaseFake
import pl.birski.falldetector.database.ContactDaoFake
import pl.birski.falldetector.model.Contact
import pl.birski.falldetector.model.util.ContactMapper

class GetAllContactsUseCaseTest {

    private val appDatabase = AppDatabaseFake()

    // system in test
    private lateinit var getAllContactsUseCase: GetAllContactsUseCase

    // dependencies
    private lateinit var contactDao: ContactDaoFake
    private val mapper = ContactMapper()

    @BeforeEach
    fun setup() {
        contactDao = ContactDaoFake(appDatabase)

        getAllContactsUseCase = GetAllContactsUseCase(
            mapper = mapper,
            contactDao = contactDao
        )
    }

    @Test
    fun getAllContactsFromDatabase(): Unit = runBlocking {

        val contacts = getAllContactsUseCase.execute().toList()

        // confirm that we received data from database
        assert(contacts.isNotEmpty())

        // confirm they are actually Contact objects
        assert(contacts.get(index = 0) is Contact)
    }

    @AfterEach
    fun tearDown() { }
}
