package nl.skbotnl.chatog.commands

import kotlinx.coroutines.launch
import net.trueog.utilitiesog.UtilitiesOG
import nl.skbotnl.chatog.ChatOG
import nl.skbotnl.chatog.ChatOG.Companion.config
import nl.skbotnl.chatog.ChatSystem
import nl.skbotnl.chatog.PlayerExtensions.chatSystem
import nl.skbotnl.chatog.chatsystem.GeneralChatSystem
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

internal abstract class ChatSystemChat<T : ChatSystem> : CommandExecutor {
    abstract val chatSystem: T
    abstract val permission: String

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender !is Player) {
            sender.sendMessage("You can only execute this command as a player.")
            return true
        }

        if (!sender.hasPermission(permission)) {
            sender.sendMessage(
                UtilitiesOG.trueogColorize(
                    "${config.prefix}<reset>: <red>You do not have permission to run this command."
                )
            )
            return true
        }

        if (args.isNullOrEmpty()) {
            if (sender.chatSystem == chatSystem) {
                sender.chatSystem = GeneralChatSystem

                sender.sendMessage(
                    UtilitiesOG.trueogColorize("${config.prefix}<reset>: You are now talking in the general chat.")
                )
                return true
            }
            sender.chatSystem = chatSystem
            sender.sendMessage(
                UtilitiesOG.trueogColorize(
                    "${config.prefix}<reset>: You are now talking in the ${chatSystem.name} chat."
                )
            )
            return true
        }

        ChatOG.scope.launch { chatSystem.sendMessage(args.joinToString(separator = " "), sender) }
        return true
    }
}
