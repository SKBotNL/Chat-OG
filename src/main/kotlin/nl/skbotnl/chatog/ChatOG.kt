package nl.skbotnl.chatog

import net.milkbowl.vault.chat.Chat
import org.bukkit.plugin.java.JavaPlugin


class ChatOG : JavaPlugin() {
    companion object {
        lateinit var plugin: JavaPlugin
        lateinit var chat: Chat
    }

    override fun onEnable() {
        plugin = this

        Config.load()
        LanguageDatabase.load()

        val rsp = server.servicesManager.getRegistration(Chat::class.java)
        chat = rsp!!.provider

        this.server.pluginManager.registerEvents(Events(), this)
        this.getCommand("translatemessage")?.setExecutor(TranslateMessage())
        this.getCommand("translatesettings")?.setExecutor(TranslateSettings())
        this.getCommand("translatesettings")?.tabCompleter = TranslateSettingsTabCompleter()
        this.getCommand("translatereload")?.setExecutor(TranslateReload())
        this.saveDefaultConfig()
    }
}