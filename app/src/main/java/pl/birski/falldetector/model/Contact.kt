package pl.birski.falldetector.model

data class Contact(val name: String, val surname: String, val prefix: String, val number: String) {

    constructor() : this("", "", "", "")
}
