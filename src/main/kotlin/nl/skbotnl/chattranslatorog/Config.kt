package nl.skbotnl.chattranslatorog

import org.bukkit.configuration.file.FileConfiguration

object Config {
    private val config: FileConfiguration = ChatTranslatorOG.plugin.config

    fun getApiKey(): String {
        return config.get("apikey").toString()
    }

    fun getSubRegion(): String {
        return config.get("subscriptionregion").toString()
    }
}