package de.theess.eisbaer.data

import io.requery.Column
import io.requery.Entity
import io.requery.Key
import io.requery.Persistable
import io.requery.Table

@Entity
@Table(name = "ZSFNOTETAG")
interface Tag : Persistable {
    @get:Key
    @get:Column(name = "Z_PK")
    var id: Long
    @get:Column(name = "ZTITLE")
    var title: String
}
