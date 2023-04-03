package nl.skbotnl.chattranslatorog

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

object Config {
    private lateinit var config: FileConfiguration
    private lateinit var file: File

    fun load() {
        file = File(ChatTranslatorOG.plugin.dataFolder, "config.yml")
        if (!file.exists()) {
            file.createNewFile()
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