package nl.skbotnl.chattranslatorog

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.*

object Config {
    private val config: FileConfiguration = ChatTranslatorOG.plugin.config

    fun getApiKey(): String {
        return config.get("apikey").toString()
    }
}