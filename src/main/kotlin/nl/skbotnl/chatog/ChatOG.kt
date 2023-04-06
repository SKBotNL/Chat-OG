package nl.skbotnl.chatog

import net.milkbowl.vault.chat.Chat
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable

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
        this.getCommand("chatconfigreload")?.setExecutor(ChatConfigReload())

        if (Config.getDiscordEnabled()) {
            object : BukkitRunnable() {
                override fun run() {
                    DiscordBridge.main()
                }
            }.runTaskAsynchronously(this)
        }
    }

    override fun onDisable() {
        if (Config.getDiscordEnabled()) {
            DiscordBridge.jda.shutdownNow()
        }
    }
}