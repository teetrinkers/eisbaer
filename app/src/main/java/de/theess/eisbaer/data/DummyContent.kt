package de.theess.eisbaer.data

import java.util.ArrayList
import java.util.HashMap

/**
 * Helper class for providing sample title for user interfaces created by
 * Android template wizards.
 *
 * TODO: Replace all uses of this class before publishing your app.
 */
object DummyContent {

    /**
     * An array of sample (dummy) items.
     */
    val ITEMS: MutableList<Note> = ArrayList()

    /**
     * A map of sample (dummy) items, by ID.
     */
    val ITEM_MAP: MutableMap<String, Note> = HashMap()

    private val COUNT = 25

    init {
        // Add some sample items.
        for (i in 1..COUNT) {
            addItem(
                createDummyItem(
                    i
                )
            )
        }
    }

    private fun addItem(item: Note) {
        ITEMS.add(item)
        ITEM_MAP.put(item.id, item)
    }

    private fun createDummyItem(position: Int): Note {
        return Note(
            position.toString(),
            "Item " + position,
            makeDetails(position)
        )
    }

    private fun makeDetails(position: Int): String {
        val builder = StringBuilder()
        builder.append("Details about Item: ").append(position)
        for (i in 0..position - 1) {
            builder.append("\n\n")
                .append("Feugiat sociis tincidunt dictum lorem gravida ornare non curabitur, dictumst orci pretium amet cubilia egestas interdum. Interdum euismod augue platea malesuada viverra felis primis, parturient nullam porttitor quis fermentum urna lacus ultricies, tempus risus tincidunt eget ornare scelerisque. Ultricies conubia mattis nisi porta aptent eleifend sed bibendum vestibulum, nibh adipiscing varius tellus mauris sapien lorem sit dolor, facilisi amet laoreet lectus interdum habitasse aliquam quis.")
        }
        return builder.toString()
    }
}
