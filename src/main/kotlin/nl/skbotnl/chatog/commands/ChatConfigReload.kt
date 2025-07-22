package nl.skbotnl.chatog.commands

import kotlin.concurrent.write
import kotlinx.coroutines.launch
import net.trueog.utilitiesog.UtilitiesOG
import nl.skbotnl.chatog.ChatOG
import nl.skbotnl.chatog.ChatOG.Companion.config
import nl.skbotnl.chatog.ChatOG.Companion.discordBridgeLock
import nl.skbotnl.chatog.ChatOG.Companion.scope
import nl.skbotnl.chatog.Config
import nl.skbotnl.chatog.DiscordBridge
import nl.skbotnl.chatog.LanguageDatabase
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

internal class ChatConfigReload : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        config =
            Config.create()
                ?: run {
                    sender.sendMessage(
                        UtilitiesOG.trueogColorize(
                            "<red>Failed to reload the config. Check the console for more information."
                        )
                    )
                    Bukkit.getPluginManager().disablePlugin(ChatOG.plugin)
                    return true
                }

        ChatOG.languageDatabase = LanguageDatabase()
        if (ChatOG.languageDatabase.testConnection()) {
            ChatOG.plugin.logger.severe("Could not connect to Redis")
            Bukkit.getPluginManager().disablePlugin(ChatOG.plugin)
            return true
        }

        if (config.discordEnabled) {
            scope.launch {
                discordBridgeLock.write {
                    val discordBridge = ChatOG.discordBridge
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
