package nl.skbotnl.chatog.commands

import club.minnced.discord.webhook.WebhookClient
import net.dv8tion.jda.api.entities.Activity
import net.trueog.utilitiesog.UtilitiesOG
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

        ChatOG.languageDatabase = LanguageDatabase()
        if (ChatOG.languageDatabase.testConnection()) {
            ChatOG.plugin.logger.severe("Could not connect to Redis")
            Bukkit.getPluginManager().disablePlugin(ChatOG.plugin)
            return true
        }

        if (Config.discordEnabled) {
            DiscordBridge.webhook = WebhookClient.withUrl(Config.webhook)
            if (Config.staffDiscordEnabled) {
                DiscordBridge.staffWebhook = WebhookClient.withUrl(Config.staffWebhook)
            }
            if (Config.premiumDiscordEnabled) {
                DiscordBridge.premiumWebhook = WebhookClient.withUrl(Config.premiumWebhook)
            }
            DiscordBridge.jda?.presence?.setPresence(Activity.playing(Config.status), false)
        }

        sender.sendMessage(UtilitiesOG.trueogColorize("${Config.prefix}<reset>: <green>Successfully reloaded the config."))
        return true
    }
}
