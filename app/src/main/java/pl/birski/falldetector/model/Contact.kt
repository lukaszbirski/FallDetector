package pl.birski.falldetector.model

data class Contact(
    val id: Int,
    val name: String,
    val surname: String,
    val prefix: String,
    val number: String
) {
    constructor() : this(0, "", "", "", "")
}
