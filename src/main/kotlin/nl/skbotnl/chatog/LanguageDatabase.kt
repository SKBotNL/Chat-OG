package nl.skbotnl.chatog

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.*

object LanguageDatabase {
    private lateinit var config: FileConfiguration
    private lateinit var file: File

    fun load() {
        file = File(ChatOG.plugin.dataFolder, "languagesdatabase.yml")
        if (!file.exists()) {
            file.createNewFile()
        }

        config = YamlConfiguration.loadConfiguration(file)

        this.save()
    }

    private fun save() {
        config.save(file)
    }

    fun setLanguage(uuid: UUID, language: String) {
        config.set(uuid.toString(), language)
        this.save()
    }

    fun getLanguage(uuid: UUID): String {
        return config.get(uuid.toString()).toString()
    }
}