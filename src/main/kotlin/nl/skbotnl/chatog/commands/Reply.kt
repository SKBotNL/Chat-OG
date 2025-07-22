package nl.skbotnl.chatog.commands

import java.util.UUID
import kotlin.collections.set
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.trueog.utilitiesog.UtilitiesOG
import nl.skbotnl.chatog.ChatOG.Companion.config
import nl.skbotnl.chatog.ChatOG.Companion.lastMessagedMap
import nl.skbotnl.chatog.Helper
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

internal class Reply : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender !is Player) {
            sender.sendMessage("You can only execute this command as a player.")
            return true
        }

        if (args == null || args.isEmpty()) {
            sender.sendMessage(
                UtilitiesOG.trueogColorize("${config.prefix}<reset>: <red>You did not provide your message.")
            )
            return true
        }

        val lastMessaged = lastMessagedMap[sender.uniqueId]
        if (lastMessaged == null) {
            sender.sendMessage(
                UtilitiesOG.trueogColorize("${config.prefix}<reset>: <red>You haven't messaged anyone yet.")
            )
            return true
        }

        val toPlayer = Bukkit.getPlayer(lastMessaged)
        if (toPlayer == null) {
            sender.sendMessage(
                UtilitiesOG.trueogColorize("${config.prefix}<reset>: <red>That player doesn't exist or isn't online.")
            )
            return true
        }

        val message = args.joinToString(" ")

        val randomUUID = UUID.randomUUID()

        TranslateMessage.pmMessages[randomUUID] =
            TranslateMessage.SentPMMessage(message, sender.uniqueId, toPlayer.uniqueId)

        val messageComponent = Helper.processText(message, sender)
        if (messageComponent == null) {
            return true
        }

        val toSenderPrefix = "<gold>[<red>me <gold>-> <dark_red>${toPlayer.name}<gold>]<white>"
        var toSenderTextComponent =
            Component.join(
                JoinConfiguration.separator(Component.text(" ")),
                UtilitiesOG.trueogColorize(toSenderPrefix),
                messageComponent,
            )
        toSenderTextComponent =
            toSenderTextComponent.hoverEvent(
                HoverEvent.hoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    UtilitiesOG.trueogColorize("<green>Click to translate this message"),
                )
            )
        toSenderTextComponent =
            toSenderTextComponent.clickEvent(
                ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/translatemessage $randomUUID 3")
            )
        sender.sendMessage(toSenderTextComponent)

        val toPrefix = "<gold>[<dark_red>${sender.name} <gold>-> <red>me<gold>]<white>"
        var toTextComponent =
            Component.join(
                JoinConfiguration.separator(Component.text(" ")),
                UtilitiesOG.trueogColorize(toPrefix),
                messageComponent,
            )
        toTextComponent =
            toTextComponent.hoverEvent(
                HoverEvent.hoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    UtilitiesOG.trueogColorize("<green>Click to translate this message"),
                )
            )
        toTextComponent =
            toTextComponent.clickEvent(
                ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/translatemessage $randomUUID 3")
            )
        toPlayer.sendMessage(toTextComponent)

        lastMessagedMap[sender.uniqueId] = toPlayer.uniqueId
        lastMessagedMap[toPlayer.uniqueId] = sender.uniqueId
        return true
    }
}
