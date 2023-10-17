package nl.skbotnl.chatog

import com.earth2me.essentials.Essentials
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.kyori.adventure.text.minimessage.MiniMessage
import net.milkbowl.vault.chat.Chat
import nl.skbotnl.chatog.commands.*
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

class ChatOG : JavaPlugin() {
    companion object {
        lateinit var plugin: JavaPlugin
        lateinit var chat: Chat
        var mm = MiniMessage.miniMessage()
        var essentials = Bukkit.getServer().pluginManager.getPlugin("Essentials") as Essentials
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onEnable() {
        plugin = this

        Config.load()
        LanguageDatabase.load()
        GlobalScope.launch {
            BlocklistManager.load()
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
        this.getCommand("dc")?.setExecutor(DonorChat())
        this.getCommand("d")?.setExecutor(DonorChat())

        if (Config.getDiscordEnabled()) {
            GlobalScope.launch {
                DiscordBridge.main()
            }
        }
    }

    override fun onDisable() {
        if (Config.getDiscordEnabled()) {
            if (DiscordBridge.jda != null) {
                DiscordBridge.sendMessageWithBot("The server has stopped <:not_stonks:939288625701617665>")
                DiscordBridge.jda!!.shutdownNow()
            }
        }
    }

    fun sendMessageWithBot(message: String) {
        DiscordBridge.sendMessageWithBot(message)
    }

    suspend fun sendMessage(message: String, player: String, uuid: UUID?) {
        DiscordBridge.sendMessage(message, player, uuid)
    }

    suspend fun sendStaffMessage(message: String, player: String, uuid: UUID?) {
        DiscordBridge.sendStaffMessage(message, player, uuid)
    }

    suspend fun sendDonorMessage(message: String, player: String, uuid: UUID?) {
        DiscordBridge.sendDonorMessage(message, player, uuid)
    }

    fun sendEmbed(message: String, uuid: UUID?, color: Int) {
        DiscordBridge.sendEmbed(message, uuid, color)
    }
}
