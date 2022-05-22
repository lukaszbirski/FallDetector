package pl.birski.falldetector.usecase

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import pl.birski.falldetector.database.AppDatabaseFake
import pl.birski.falldetector.database.ContactDaoFake
import pl.birski.falldetector.model.Contact
import pl.birski.falldetector.model.util.ContactMapper

class AddContactUseCaseTest {

    private val appDatabase = AppDatabaseFake()

    // system in test
    private lateinit var addContactUseCase: AddContactUseCase

    // dependencies
    private lateinit var contactDao: ContactDaoFake
    private lateinit var mapper: ContactMapper

    private lateinit var getAllContactsUseCase: GetAllContactsUseCase

    // want to add element to database
    private val newContact = Contact(
        id = 3,
        name = "Robert",
        surname = "Johnsson",
        prefix = "+15",
        number = "121212121"
    )

    @BeforeEach
    fun setup() {
        contactDao = ContactDaoFake(appDatabase)

        mapper = ContactMapper()

        addContactUseCase = AddContactUseCase(
            mapper = mapper,
            contactDao = contactDao
        )

        getAllContactsUseCase = GetAllContactsUseCase(
            mapper = mapper,
            contactDao = contactDao
        )
    }

    @Test
    fun addContactToDatabase(): Unit = runBlocking {

        val contacts = getAllContactsUseCase.execute().toList()

        // confirm that database do not contain element
        assert(newContact !in contacts)

        addContactUseCase.execute(newContact)

        val contactList = getAllContactsUseCase.execute().toList()

        // confirm that database contain element after it was added to database
        assert(newContact in contactList)
    }

    @AfterEach
    fun tearDown() {
    }
}
