package nl.skbotnl.chatog.commands

import club.minnced.discord.webhook.WebhookClient
import net.dv8tion.jda.api.entities.Activity
import nl.skbotnl.chatog.ChatOG
import nl.skbotnl.chatog.Config
import nl.skbotnl.chatog.DiscordBridge
import nl.skbotnl.chatog.LanguageDatabase
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class ChatConfigReload : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (Config.load()) {
            sender.sendMessage(UtilitiesOG.trueogColorize("<red>Failed to reload the config. Check the console for more information."))
            Bukkit.getPluginManager().disablePlugin(ChatOG.plugin)
            return true
        }

        LanguageDatabase.init()

        DiscordBridge.webhook = WebhookClient.withUrl(Config.webhook)
        DiscordBridge.staffWebhook = WebhookClient.withUrl(Config.staffWebhook)
        DiscordBridge.premiumWebhook = WebhookClient.withUrl(Config.premiumWebhook)
        DiscordBridge.jda?.presence?.setPresence(Activity.playing(Config.status), false)

        sender.sendMessage(UtilitiesOG.trueogColorize("${Config.prefix}<reset>: <green>Successfully reloaded the config."))
        return true
    }
}
