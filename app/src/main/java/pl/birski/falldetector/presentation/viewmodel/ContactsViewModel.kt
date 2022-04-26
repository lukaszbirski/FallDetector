package pl.birski.falldetector.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import kotlinx.coroutines.launch
import pl.birski.falldetector.model.Contact
import pl.birski.falldetector.usecase.UseCaseFactory
import timber.log.Timber

@HiltViewModel
class ContactsViewModel
@Inject
constructor(
    private val useCaseFactory: UseCaseFactory
) : ViewModel() {

    private var contact = Contact()

    private val _contacts = MutableLiveData<ArrayList<Contact>>()
    val contacts: LiveData<ArrayList<Contact>> get() = _contacts

    init {
        getAllContacts()
    }

    fun setContactData(contact: Contact) {
        this.contact = contact
    }

    fun getContact() = contact

    fun addContact() {
        addPlusToPrefix()
        viewModelScope.launch(Dispatchers.IO) {
            useCaseFactory.addDriverUseCase.execute(contact)
            getAllContacts()
        }
    }

    private fun addPlusToPrefix() {
        contact = contact.copy(prefix = "+${contact.prefix}")
    }

    private fun getAllContacts() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = useCaseFactory.getAllContactsUseCase.execute() as ArrayList
            Timber.d("Got ${result.size} contacts from database")
            _contacts.postValue(result)
        }
    }

    private fun removeContact(contact: Contact) {
        viewModelScope.launch(Dispatchers.IO) {
            useCaseFactory.removeContactUseCase.execute(contact)
        }
    }

    fun enableButton() =
        contact.name.isNotBlank() &&
            contact.surname.isNotBlank() &&
            contact.prefix.isNotBlank() &&
            contact.number.isNotBlank()
}
