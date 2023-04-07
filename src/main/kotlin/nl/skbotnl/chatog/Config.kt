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
        return config.get("apiKey").toString()
    }

    fun getSubRegion(): String {
        return config.get("subscriptionRegion").toString()
    }

    fun getDiscordEnabled(): Boolean {
        return config.get("discordEnabled").toString().toBoolean()
    }

    fun getBotToken(): String {
        return config.get("botToken").toString()
    }

    fun getChannelId(): String {
        return config.get("channelId").toString()
    }

    fun getWebhook(): String {
        return config.get("webhook").toString()
    }

    fun getRoles(): MutableSet<String>? {
        return config.getConfigurationSection("roles")?.getKeys(false) ?: return null
    }

    fun getRoleMessageColor(role: String): Any {
        val messageColor = config.getStringList("roles.$role.message_color")
        if (messageColor.isEmpty()) {
            return config.get("roles.$role.message_color").toString()
        }
        return messageColor
    }
}