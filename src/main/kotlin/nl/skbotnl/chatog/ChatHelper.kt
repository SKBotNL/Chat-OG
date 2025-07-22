package nl.skbotnl.chatog

import java.util.UUID
import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.trueog.utilitiesog.UtilitiesOG
import nl.skbotnl.chatog.Helper.legacyToMm
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Player

internal object ChatHelper {
    fun getPlayerPart(player: Player, addSuffix: Boolean): TextComponent {
        val playerPartString = getPlayerPartString(player)

        val suffix =
            if (addSuffix) {
                PlayerAffix.getSuffix(player.uniqueId)
            } else {
                ""
            }

        return UtilitiesOG.trueogColorize(legacyToMm("$playerPartString<reset>$suffix"))
    }

    fun getPlayerPartString(player: Player): String {
        var playerPart = "${PlayerAffix.getPrefix(player.uniqueId)}${player.name}"

        if (PlaceholderAPI.setPlaceholders(player, "%simpleclans_clan_color_tag%") != "") {
            playerPart = PlaceholderAPI.setPlaceholders(player, "&8[%simpleclans_clan_color_tag%&8] $playerPart")
        }

        return playerPart
    }

    val mentionRegex = Regex("@([A-Za-z0-9_]{3,16})")

    fun dingForMentions(dontDingFor: UUID, component: Component) {
        val messageContent = PlainTextComponentSerializer.plainText().serialize(component)

        val namesToMention = mutableListOf<String>()
        mentionRegex.findAll(messageContent).iterator().forEach { namesToMention += it.groups[1]!!.value.lowercase() }

        for (player in Bukkit.getOnlinePlayers()) {
            if (player.uniqueId == dontDingFor) continue
            if (player.name.lowercase() !in namesToMention) continue
            player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)
        }
    }
}
