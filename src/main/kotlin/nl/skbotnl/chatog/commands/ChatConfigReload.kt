package nl.skbotnl.chatog.commands

import club.minnced.discord.webhook.WebhookClient
import net.dv8tion.jda.api.entities.Activity
import nl.skbotnl.chatog.BingTranslator
import nl.skbotnl.chatog.Config
import nl.skbotnl.chatog.DiscordBridge
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
        DiscordBridge.staffChannelId = Config.getStaffChannelId()
        DiscordBridge.donorChannelId = Config.getDonorChannelId()
        DiscordBridge.guildId = Config.getGuildId()
        DiscordBridge.webhook = WebhookClient.withUrl(Config.getWebhook())
        DiscordBridge.staffWebhook = WebhookClient.withUrl(Config.getStaffWebhook())
        DiscordBridge.donorWebhook = WebhookClient.withUrl(Config.getDonorWebhook())
        DiscordBridge.jda?.presence?.setPresence(Activity.playing(Config.getStatus()), false)

        sender.sendMessage(convertColor("&aReloaded the config!"))
        return true
    }
}