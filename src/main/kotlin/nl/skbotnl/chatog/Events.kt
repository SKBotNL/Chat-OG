package nl.skbotnl.chatog

import io.papermc.paper.event.player.AsyncChatEvent
import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import nl.skbotnl.chatog.Helper.convertColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.util.*

class Events : Listener {
    @EventHandler
    fun chatEvent(event: AsyncChatEvent) {
        event.isCancelled = true

        val oldTextComponent = event.message() as TextComponent

        var chatString = "${ChatOG.chat.getPlayerPrefix(event.player)} ${event.player.name} ${ChatOG.chat.getPlayerSuffix(event.player)}> ${oldTextComponent.content()}"

        if (PlaceholderAPI.setPlaceholders(event.player, "%parties_party%") != "") {
            chatString = PlaceholderAPI.setPlaceholders(event.player, "&8[%parties_color_code%%parties_party%&8] $chatString")
        }
        chatString = convertColor(chatString)

        var textComponent = Component.text(chatString)
        textComponent = textComponent.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text(convertColor("&aClick to translate this message"))))

        val randomUUID = UUID.randomUUID()
        textComponent = textComponent.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/translatemessage $randomUUID"))

        event.viewers().forEach {
            it.sendMessage(textComponent)
        }
        TranslateMessage.messages[randomUUID] = TranslateMessage.SentMessage(oldTextComponent, event.player)
    }
}