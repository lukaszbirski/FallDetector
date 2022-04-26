package pl.birski.falldetector.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
class ContactEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int?,

    @ColumnInfo(name = "name")
    var name: String,

    @ColumnInfo(name = "surname")
    var surname: String,

    @ColumnInfo(name = "prefix")
    var prefix: String,

    @ColumnInfo(name = "number")
    var number: String
)
