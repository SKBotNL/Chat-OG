package nl.skbotnl.chatog

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

object Config {
    private lateinit var config: FileConfiguration
    private lateinit var file: File

    fun load() {
        file = File(ChatOG.plugin.dataFolder, "config.yml")
        if (!file.exists()) {
            ChatOG.plugin.saveDefaultConfig()
        }

        config = YamlConfiguration.loadConfiguration(file)

        this.save()
    }

    private fun save() {
        config.save(file)
    }

    fun getApiKey(): String {
        return config.get("apikey").toString()
    }

    fun getSubRegion(): String {
        return config.get("subscriptionregion").toString()
    }
}