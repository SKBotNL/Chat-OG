package nl.skbotnl.chatog.commands

import net.trueog.utilitiesog.UtilitiesOG
import nl.skbotnl.chatog.ChatSystemHelper
import nl.skbotnl.chatog.ChatSystemHelper.ChatType
import nl.skbotnl.chatog.Config
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class StaffChat : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender !is Player) {
            sender.sendMessage("You can only execute this command as a player")
            return true
        }
        if (!sender.hasPermission("chat-og.staff")) {
            sender.sendMessage(
                UtilitiesOG.trueogColorize(
                    "${Config.prefix}<reset>: <red>You do not have permission to run this command."
                )
            )
            return true
        }
        if (args == null || args.isEmpty()) {
            if (ChatSystemHelper.inChat[sender.uniqueId] == ChatType.STAFFCHAT) {
                ChatSystemHelper.inChat[sender.uniqueId] = ""

                sender.sendMessage(
                    UtilitiesOG.trueogColorize("${Config.prefix}<reset>: You are now talking in normal chat.")
                )
                return true
            }
            ChatSystemHelper.inChat[sender.uniqueId] = ChatType.STAFFCHAT
            sender.sendMessage(
                UtilitiesOG.trueogColorize("${Config.prefix}<reset>: You are now talking in staff chat.")
            )
            return true
        }

        ChatSystemHelper.sendMessageInStaffChat(sender, args.joinToString(separator = " "))

        return true
    }
}
