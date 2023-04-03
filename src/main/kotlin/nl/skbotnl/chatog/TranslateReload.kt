package nl.skbotnl.chatog

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class TranslateReload : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        Config.load()
        BingTranslator.apiKey = Config.getApiKey()
        BingTranslator.subscriptionRegion = Config.getSubRegion()
        return true
    }
}