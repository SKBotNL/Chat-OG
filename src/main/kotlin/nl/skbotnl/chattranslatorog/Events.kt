package nl.skbotnl.chattranslatorog

import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.util.*


class Events : Listener {
    @EventHandler
    fun chatEvent(event: AsyncChatEvent) {
        event.isCancelled = true

        val oldTextComponent = event.message() as TextComponent
        var textComponent = Component.text("<${event.player.name}> ${oldTextComponent.content()}")
        textComponent = textComponent.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("Click to translate the message")))

        val randomUUID = UUID.randomUUID()
        textComponent = textComponent.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/translatemessage $randomUUID"))

        event.viewers().forEach {
            it.sendMessage(textComponent)
        }
        TranslateMessage.messages[randomUUID] = TranslateMessage.SentMessage(oldTextComponent, event.player)
    }
}