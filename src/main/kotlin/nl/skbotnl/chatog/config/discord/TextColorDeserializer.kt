package nl.skbotnl.chatog.config.discord

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ValueDeserializer

class TextColorDeserializer : ValueDeserializer<TextColor>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): TextColor? {
        val node: JsonNode = ctxt.readTree(p)

        return when {
            node.isValueNode ->
                NamedTextColor.NAMES.value(node.stringValue().lowercase())
                    ?: throw IllegalArgumentException("Unknown color: ${node.stringValue()}")
            node.isArray && node.size() == 3 -> {
                val r = node[0].asInt()
                val g = node[1].asInt()
                val b = node[2].asInt()
                TextColor.color(r, g, b)
            }
            else -> throw IllegalArgumentException("Invalid color format: $node")
        }
    }
}
