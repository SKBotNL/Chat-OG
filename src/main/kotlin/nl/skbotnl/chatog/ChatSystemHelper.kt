package nl.skbotnl.chatog

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.trueog.utilitiesog.UtilitiesOG
import nl.skbotnl.chatog.Helper.legacyToMm
import nl.skbotnl.chatog.Helper.removeColor
import nl.skbotnl.chatog.commands.TranslateMessage
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

@OptIn(DelicateCoroutinesApi::class)
object ChatSystemHelper {
    object ChatType {
        const val STAFFCHAT = "staffchat"
        const val PREMIUMCHAT = "premiumchat"
    }

    var inChat: MutableMap<UUID, String> = HashMap()

    fun sendMessageInStaffChat(player: Player, text: String) {
        var chatString = "${ChatOG.chat.getPlayerPrefix(player)}${player.name}"

        if (PlaceholderAPI.setPlaceholders(player, "%simpleclans_clan_color_tag%") != "") {
            chatString = PlaceholderAPI.setPlaceholders(player, "&8[%simpleclans_clan_color_tag%&8] $chatString")
        }
        chatString = "&cSTAFF | $chatString"

        if (Config.staffDiscordEnabled) {
            val discordMessageString = Helper.convertEmojis(text)

            ChatOG.scope.launch {
                DiscordBridge.sendStaffMessage(discordMessageString, removeColor(chatString), player.uniqueId)
            }
        }

        val messageComponents = Helper.convertLinks(text, player)

        val messageComponent =
            Component.join(JoinConfiguration.separator(Component.text(" ")), messageComponents) as TextComponent

        val chatComponent = UtilitiesOG.trueogColorize(legacyToMm("$chatString${ChatOG.chat.getPlayerSuffix(player)}"))

        var textComponent =
            Component.join(JoinConfiguration.noSeparators(), chatComponent, messageComponent)
        textComponent = textComponent.hoverEvent(
            HoverEvent.hoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                UtilitiesOG.trueogColorize("<green>Click to translate this message")
            )
        )

        val randomUUID = UUID.randomUUID()
        textComponent = textComponent.clickEvent(
            ClickEvent.clickEvent(
                ClickEvent.Action.RUN_COMMAND,
                "/translatemessage $randomUUID 1"
            )
        )

        TranslateMessage.chatMessages[randomUUID] = TranslateMessage.SentChatMessage(text, player)

        for (p in Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("chat-og.staff")) {
                p.sendMessage(textComponent)
            }
        }
    }

    fun sendMessageInPremiumChat(player: Player, text: String) {
        var chatString = "${ChatOG.chat.getPlayerPrefix(player)}${player.name}"

        if (PlaceholderAPI.setPlaceholders(player, "%simpleclans_clan_color_tag%") != "") {
            chatString = PlaceholderAPI.setPlaceholders(player, "&8[%simpleclans_clan_color_tag%&8] $chatString")
        }
        chatString = "&aPREMIUM | $chatString"

        if (Config.premiumDiscordEnabled) {
            val discordMessageString = Helper.convertEmojis(text)

            ChatOG.scope.launch {
                DiscordBridge.sendPremiumMessage(discordMessageString, removeColor(chatString), player.uniqueId)
            }
        }

        val messageComponents = Helper.convertLinks(text, player)

        val messageComponent =
            Component.join(JoinConfiguration.separator(Component.text(" ")), messageComponents) as TextComponent

        val chatComponent = UtilitiesOG.trueogColorize(legacyToMm("$chatString${ChatOG.chat.getPlayerSuffix(player)}"))

        var textComponent =
            Component.join(JoinConfiguration.noSeparators(), chatComponent, messageComponent)
        textComponent = textComponent.hoverEvent(
            HoverEvent.hoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                UtilitiesOG.trueogColorize("<green>Click to translate this message")
            )
        )

        val randomUUID = UUID.randomUUID()
        textComponent = textComponent.clickEvent(
            ClickEvent.clickEvent(
                ClickEvent.Action.RUN_COMMAND,
                "/translatemessage $randomUUID 1"
            )
        )

        TranslateMessage.chatMessages[randomUUID] = TranslateMessage.SentChatMessage(text, player)

        for (p in Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("chat-og.premium")) {
                p.sendMessage(textComponent)
            }
        }
    }
}
