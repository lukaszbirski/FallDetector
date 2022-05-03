package pl.birski.falldetector.database

import pl.birski.falldetector.database.model.ContactEntity

class AppDatabaseFake {
    val contacts = mutableListOf<ContactEntity>()
}
