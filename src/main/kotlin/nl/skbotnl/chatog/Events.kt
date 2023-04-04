package nl.skbotnl.chatog

import io.papermc.paper.event.player.AsyncChatEvent
import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import nl.skbotnl.chatog.Helper.convertColor
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import java.util.*

class Events : Listener {
    private var lastMessaged: MutableMap<UUID, UUID> = HashMap()

    @EventHandler
    fun chatEvent(event: AsyncChatEvent) {
        event.isCancelled = true

        val oldTextComponent = event.message() as TextComponent

        var chatString = "${ChatOG.chat.getPlayerPrefix(event.player)}${event.player.name}${ChatOG.chat.getPlayerSuffix(event.player)}"

        if (PlaceholderAPI.setPlaceholders(event.player, "%parties_party%") != "") {
            chatString = PlaceholderAPI.setPlaceholders(event.player, "&8[%parties_color_code%%parties_party%&8] $chatString")
        }
        chatString = convertColor(chatString)
        chatString = "$chatString${oldTextComponent.content()}"

        if (event.player.hasPermission("chat-og.color")) {
            chatString = convertColor(chatString)
        }

        var textComponent = Component.text(chatString)
        textComponent = textComponent.hoverEvent(
            HoverEvent.hoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                Component.text(convertColor("&aClick to translate this message"))
            )
        )

        val randomUUID = UUID.randomUUID()
        textComponent = textComponent.clickEvent(
            ClickEvent.clickEvent(
                ClickEvent.Action.RUN_COMMAND,
                "/translatemessage $randomUUID"
            )
        )

        event.viewers().forEach {
            it.sendMessage(textComponent)
        }
        TranslateMessage.messages[randomUUID] = TranslateMessage.SentMessage(oldTextComponent, event.player)
    }

    @EventHandler
    fun commandPreprocess(event: PlayerCommandPreprocessEvent) {
        val checkSplit = event.message.split(" ", ignoreCase = true, limit = 2)[0]

        if (!(checkSplit == "/msg" || checkSplit == "/whisper" || checkSplit == "/pm" || checkSplit == "/reply" || checkSplit == "/r")) {
            return
        }
        event.isCancelled = true

        val messageSplit = event.message.split(" ", ignoreCase = true, limit = 3)
        val checkCount: Int = if (checkSplit == "/r" || checkSplit == "/reply") {
            2
        } else {
            3
        }

        if (messageSplit.count() < checkCount) {
            if (checkSplit == "/r" || checkSplit == "/reply") {
                event.player.sendMessage(convertColor("&c${messageSplit[0]} <message>"))
            }
            else {
                event.player.sendMessage(convertColor("&c${messageSplit[0]} <to> <message>"))
            }
            return
        }

        val player: Player?
        var message: String

        if (checkSplit == "/r" || checkSplit == "/reply") {
            message = messageSplit[1]

            if (!lastMessaged.containsKey(event.player.uniqueId)) {
                event.player.sendMessage(convertColor("&cYou haven't messaged anyone yet"))
                return
            }

            val lastMessagedPlayer = lastMessaged[event.player.uniqueId]
            player = Bukkit.getPlayer(lastMessagedPlayer!!)
        }
        else {
            player = Bukkit.getPlayer(messageSplit[1])
            message = messageSplit[2]
        }

        if (player == null) {
            event.player.sendMessage(convertColor("&cThat player doesn't exist or isn't online"))
            return
        }

        var textComponent = Component.text()
        textComponent = textComponent.hoverEvent(
            HoverEvent.hoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                Component.text(convertColor("&aClick to translate this message"))
            )
        )

        val randomUUID = UUID.randomUUID()
        textComponent = textComponent.clickEvent(
            ClickEvent.clickEvent(
                ClickEvent.Action.RUN_COMMAND,
                "/translatemessage $randomUUID"
            )
        )

        TranslateMessage.messages[randomUUID] = TranslateMessage.SentMessage(Component.text(message), event.player)

        if (event.player.hasPermission("chat-og.color")) {
            message = convertColor(message)
        }

        var toSenderPrefix = "&6[&cme &6-> &4${player.name}&6]&f"
        toSenderPrefix = convertColor(toSenderPrefix)
        textComponent.content("$toSenderPrefix $message")
        event.player.sendMessage(textComponent)

        var toPrefix = "&6[&4${event.player.name} &6-> &cme&6]&f"
        toPrefix = convertColor(toPrefix)
        textComponent.content("$toPrefix $message")
        player.sendMessage(textComponent)

        lastMessaged[event.player.uniqueId] = player.uniqueId
        lastMessaged[player.uniqueId] = event.player.uniqueId

        return
    }
}