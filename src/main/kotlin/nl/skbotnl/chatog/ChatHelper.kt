package nl.skbotnl.chatog

import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.text.TextComponent
import net.trueog.utilitiesog.UtilitiesOG
import nl.skbotnl.chatog.Helper.legacyToMm
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
}
