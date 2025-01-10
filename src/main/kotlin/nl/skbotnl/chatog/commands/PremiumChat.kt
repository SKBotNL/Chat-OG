package nl.skbotnl.chatog.commands

import nl.skbotnl.chatog.ChatOG
import nl.skbotnl.chatog.ChatSystemHelper
import nl.skbotnl.chatog.ChatSystemHelper.ChatType
import nl.skbotnl.chatog.Config
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class PremiumChat : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender !is Player) {
            sender.sendMessage("You can only execute this command as a player.")
            return true
        }
        if (!sender.hasPermission("chat-og.premium")) {
            sender.sendMessage(ChatOG.mm.deserialize("${Config.prefix}<reset>: <red>You do not have permission to run this command."))
            return true
        }
        if (args == null || args.isEmpty()) {
            if (ChatSystemHelper.inChat[sender.uniqueId] == ChatType.PREMIUMCHAT) {
                ChatSystemHelper.inChat[sender.uniqueId] = ""

                sender.sendMessage(ChatOG.mm.deserialize("${Config.prefix}<reset>: You are now talking in normal chat."))
                return true
            }
            ChatSystemHelper.inChat[sender.uniqueId] = ChatType.PREMIUMCHAT
            sender.sendMessage(ChatOG.mm.deserialize("${Config.prefix}<reset>: You are now talking in premium chat."))
            return true
        }

        ChatSystemHelper.sendMessageInPremiumChat(sender, args.joinToString(separator = " "))

        return true
    }
}
