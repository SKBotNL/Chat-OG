package nl.skbotnl.chatog.config.command

import kotlin.concurrent.write
import kotlinx.coroutines.launch
import net.trueog.utilitiesog.UtilitiesOG
import nl.skbotnl.chatog.ChatOG
import nl.skbotnl.chatog.ChatOG.Companion.blocklistManager
import nl.skbotnl.chatog.ChatOG.Companion.config
import nl.skbotnl.chatog.ChatOG.Companion.discordBridge
import nl.skbotnl.chatog.ChatOG.Companion.discordBridgeLock
import nl.skbotnl.chatog.ChatOG.Companion.languageDatabase
import nl.skbotnl.chatog.ChatOG.Companion.plugin
import nl.skbotnl.chatog.ChatOG.Companion.scope
import nl.skbotnl.chatog.ChatOG.Companion.translator
import nl.skbotnl.chatog.config.Config
import nl.skbotnl.chatog.discord.DiscordBridge
import nl.skbotnl.chatog.translation.LanguageDatabase
import nl.skbotnl.chatog.translation.OpenAITranslator
import nl.skbotnl.chatog.util.BlocklistManager
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

internal class ChatConfigReload : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        config =
            Config.loadConfig()
                ?: run {
                    sender.sendMessage(
                        UtilitiesOG.trueogColorize(
                            "<red>Failed to reload the config. Check the console for more information."
                        )
                    )
                    Bukkit.getPluginManager().disablePlugin(plugin)
                    return true
                }

        if (config.blocklist.enabled) {
            blocklistManager = BlocklistManager()
        }

        translator =
            if (config.openai.enabled) {
                if (config.openai.baseUrl == null) {
                    plugin.logger.warning(
                        "You have enabled OpenAI translation but have not set up the base url, not enabling the translator"
                    )
                    null
                } else if (config.openai.apiKey == null) {
                    plugin.logger.warning(
                        "You have enabled OpenAI translation but have not set up the api key, not enabling the translator"
                    )
                    null
                } else {
                    OpenAITranslator()
                }
            } else null

        languageDatabase = LanguageDatabase()
        if (languageDatabase.testConnection()) {
            plugin.logger.severe("Could not connect to Redis")
            Bukkit.getPluginManager().disablePlugin(plugin)
            return true
        }

        if (config.discord.enabled) {
            scope.launch {
                discordBridgeLock.write {
                    val discordBridge = discordBridge
                    if (discordBridge != null) {
                        discordBridge.sendMessageWithBot("Reloading the config...")
                        discordBridge.shutdownNow()
                    }
                    ChatOG.discordBridge = DiscordBridge.create()
                    sender.sendMessage(
                        UtilitiesOG.trueogColorize("${config.prefix}<reset>: <green>Successfully reloaded the config.")
                    )
                }
            }
        } else {
            sender.sendMessage(
                UtilitiesOG.trueogColorize("${config.prefix}<reset>: <green>Successfully reloaded the config.")
            )
        }
        return true
    }
}
