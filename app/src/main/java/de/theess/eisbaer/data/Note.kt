package de.theess.eisbaer.data

import io.requery.*

@Entity
@Table(name = "ZSFNOTE")
interface Note : Persistable {
    @get:Key
    @get:Column(name = "Z_PK")
    var id: Long
    @get:Column(name = "ZTITLE")
    var title: String
    @get:Column(name = "ZSUBTITLE")
    var subtitle: String
    @get:Column(name = "ZTEXT")
    var content: String

    /**
     * Unix timestamp adjusted by 31 years
     * https://apple.stackexchange.com/questions/288318/what-format-does-the-apple-notes-sqlite-db-date-use
     */
    // @get:Column(name = "datetime(ZMODIFICATIONDATE, 'unixepoch', '31 years', 'localtime')")
    // var modificationDate: String

    @get:Column(name = "ZMODIFICATIONDATE")
    var modificationDateRaw: String
}
