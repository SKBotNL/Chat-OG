package nl.skbotnl.chattranslatorog

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.*

object LanguageDatabase {
    private lateinit var config: FileConfiguration
    private lateinit var file: File

    fun load() {
        this.file = File(ChatTranslatorOG.plugin.dataFolder, "languagesdatabase.yml")
        if (!this.file.exists()) {
            this.file.createNewFile()
        }

        config = YamlConfiguration.loadConfiguration(this.file)

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