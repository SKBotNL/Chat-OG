package nl.skbotnl.chatog

import com.earth2me.essentials.Essentials
import kotlinx.coroutines.*
import net.milkbowl.vault.chat.Chat
import nl.skbotnl.chatog.commands.*
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

class ChatOG : JavaPlugin() {
    @OptIn(DelicateCoroutinesApi::class)
    companion object {
        val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

        lateinit var plugin: JavaPlugin
        lateinit var chat: Chat
        var translator: OpenAI? = null
        var essentials = Bukkit.getServer().pluginManager.getPlugin("Essentials") as Essentials

        // API
        @JvmStatic
        @Suppress("unused")
        fun sendMessageWithBot(message: String) {
            scope.launch { DiscordBridge.sendMessageWithBot(message) }
        }

        @JvmStatic
        @Suppress("unused")
        fun sendMessage(message: String, player: String, uuid: UUID?) {
            scope.launch { DiscordBridge.sendMessage(message, player, uuid) }
        }

        @JvmStatic
        @Suppress("unused")
        fun sendStaffMessage(message: String, player: String, uuid: UUID?) {
            scope.launch { DiscordBridge.sendStaffMessage(message, player, uuid) }
        }

        @JvmStatic
        @Suppress("unused")
        fun sendPremiumMessage(message: String, player: String, uuid: UUID?) {
            scope.launch { DiscordBridge.sendPremiumMessage(message, player, uuid) }
        }

        @JvmStatic
        @Suppress("unused")
        fun sendEmbed(message: String, uuid: UUID?, color: Int) {
            scope.launch { DiscordBridge.sendEmbed(message, uuid, color) }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onEnable() {
        plugin = this

        if (Config.load()) {
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }
        translator = if (Config.openAIEnabled) {
            OpenAI()
        } else null

        LanguageDatabase.init()
        BlocklistManager.load()
        scope.launch {
            EmojiConverter.load()
        }

        val rsp = server.servicesManager.getRegistration(Chat::class.java)
        chat = rsp!!.provider

        this.server.pluginManager.registerEvents(Events(), this)
        this.getCommand("translatemessage")?.setExecutor(TranslateMessage())
        this.getCommand("translatesettings")?.setExecutor(TranslateSettings())
        this.getCommand("translatesettings")?.tabCompleter = TranslateSettingsTabCompleter()
        this.getCommand("chatconfigreload")?.setExecutor(ChatConfigReload())
        this.getCommand("sc")?.setExecutor(StaffChat())
        this.getCommand("p")?.setExecutor(PremiumChat())

        if (Config.discordEnabled) {
            scope.launch {
                DiscordBridge.main()
            }
        }
    }

    override fun onDisable() {
        if (Config.discordEnabled) {
            if (DiscordBridge.jda != null) {
                DiscordBridge.sendMessageWithBot(Config.serverHasStoppedMessage)
                DiscordBridge.jda!!.shutdownNow()
            }
        }

        scope.cancel()

        runBlocking {
            scope.coroutineContext[Job]?.join()
        }
    }
}
