package pl.birski.falldetector.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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

    private val _contacts = MutableLiveData<List<Contact>>()
    val contacts: LiveData<List<Contact>> get() = _contacts

    init {
        getAllContacts()
    }

    fun setContactData(contact: Contact) {
        this.contact = contact
    }

    fun getContact() = contact

    fun addContact() {
        addPlusToPrefix()
        viewModelScope.launch {
            useCaseFactory.addDriverUseCase.execute(contact)
        }
    }

    private fun addPlusToPrefix() {
        contact = contact.copy(prefix = "+${contact.prefix}")
    }

    private fun getAllContacts() {
        viewModelScope.launch {
            val result = useCaseFactory.getAllContactsUseCase.execute()
            Timber.d("Got ${result.size} contacts from database")
            _contacts.postValue(result)
        }
    }

    fun enableButton() =
        contact.name.isNotBlank() &&
            contact.surname.isNotBlank() &&
            contact.prefix.isNotBlank() &&
            contact.number.isNotBlank()
}
