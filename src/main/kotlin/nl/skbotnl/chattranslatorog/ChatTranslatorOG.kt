package nl.skbotnl.chattranslatorog

import org.bukkit.plugin.java.JavaPlugin

class ChatTranslatorOG : JavaPlugin() {
    companion object {
        lateinit var plugin: JavaPlugin
    }

    override fun onEnable() {
        plugin = this
        Config.load()
        LanguageDatabase.load()

        this.server.pluginManager.registerEvents(Events(), this)
        this.getCommand("translatemessage")?.setExecutor(TranslateMessage())
        this.getCommand("translatesettings")?.setExecutor(TranslateSettings())
        this.getCommand("translatesettings")?.tabCompleter = TranslateSettingsTabCompleter()
        this.getCommand("translatereload")?.setExecutor(TranslateReload())
        this.saveDefaultConfig()
    }
}