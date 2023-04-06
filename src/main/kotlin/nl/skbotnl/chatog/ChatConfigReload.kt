package nl.skbotnl.chatog

import nl.skbotnl.chatog.Helper.convertColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class ChatConfigReload : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        Config.load()
        BingTranslator.apiKey = Config.getApiKey()
        BingTranslator.subscriptionRegion = Config.getSubRegion()

        DiscordBridge.channelId = Config.getChannelId()

        sender.sendMessage(convertColor("&aReloaded the config!"))
        return true
    }
}