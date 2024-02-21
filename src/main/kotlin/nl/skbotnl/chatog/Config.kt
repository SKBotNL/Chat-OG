package nl.skbotnl.chatog

import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import kotlin.properties.Delegates

object Config {
    lateinit var prefix: String
    lateinit var redisUrl: String
    var discordEnabled by Delegates.notNull<Boolean>()
    var staffDiscordEnabled by Delegates.notNull<Boolean>()
    var donorDiscordEnabled by Delegates.notNull<Boolean>()
    lateinit var status: String
    lateinit var serverHasStartedMessage: String
    lateinit var serverHasStoppedMessage: String
    lateinit var botToken: String
    lateinit var channelId: String
    lateinit var staffChannelId: String
    lateinit var donorChannelId: String
    lateinit var guildId: String
    lateinit var webhook: String
    lateinit var staffWebhook: String
    lateinit var donorWebhook: String
    lateinit var listCommandName: String
    lateinit var listCommandText: String
    lateinit var colorCodeRoles: List<String>
    lateinit var roles: Set<String>
    lateinit var roleMessageColor: MutableMap<String, Any>

    data class RGBColor(val r: Int, val g: Int, val b: Int)

    fun load(): Boolean {
        val file = File(ChatOG.plugin.dataFolder, "config.yml")
        if (!file.exists()) {
            ChatOG.plugin.saveDefaultConfig()
        }
        val config = YamlConfiguration.loadConfiguration(file)
        config.save(file)

        try {
            prefix = config.get("prefix") as String
        } catch (_: Exception) {
            ChatOG.plugin.logger.severe("Failed to parse config option \"prefix\" as a string")
            return true
        }

        try {
            redisUrl = config.get("redisUrl") as String
        } catch (_: Exception) {
            ChatOG.plugin.logger.severe("Failed to parse config option \"redisUrl\" as a string")
            return true
        }

        try {
            discordEnabled = config.get("discordEnabled") as Boolean
        } catch (_: Exception) {
            ChatOG.plugin.logger.severe("Failed to parse config option \"discordEnabled\" as a boolean")
            return true
        }

        try {
            staffDiscordEnabled = config.get("staffDiscordEnabled") as Boolean
        } catch (_: Exception) {
            ChatOG.plugin.logger.severe("Failed to parse config option \"staffDiscordEnabled\" as a boolean")
            return true
        }

        try {
            donorDiscordEnabled = config.get("donorDiscordEnabled") as Boolean
        } catch (_: Exception) {
            ChatOG.plugin.logger.severe("Failed to parse config option \"donorDiscordEnabled\" as a boolean")
            return true
        }

        try {
            status = config.get("status") as String
        } catch (_: Exception) {
            ChatOG.plugin.logger.severe("Failed to parse config option \"status\" as a string")
            return true
        }

        try {
            serverHasStartedMessage = config.get("serverHasStartedMessage") as String
        } catch (_: Exception) {
            ChatOG.plugin.logger.severe("Failed to parse config option \"serverHasStartedMessage\" as a string")
            return true
        }

        try {
            serverHasStoppedMessage = config.get("serverHasStoppedMessage") as String
        } catch (_: Exception) {
            ChatOG.plugin.logger.severe("Failed to parse config option \"serverHasStoppedMessage\" as a string")
            return true
        }

        try {
            botToken = config.get("botToken") as String
        } catch (_: Exception) {
            ChatOG.plugin.logger.severe("Failed to parse config option \"botToken\" as a string")
            return true
        }

        try {
            channelId = config.get("channelId") as String
        } catch (_: Exception) {
            ChatOG.plugin.logger.severe("Failed to parse config option \"channelId\" as a string")
            return true
        }

        try {
            staffChannelId = config.get("staffChannelId") as String
        } catch (_: Exception) {
            ChatOG.plugin.logger.severe("Failed to parse config option \"staffChannelId\" as a string")
            return true
        }

        try {
            donorChannelId = config.get("donorChannelId") as String
        } catch (_: Exception) {
            ChatOG.plugin.logger.severe("Failed to parse config option \"donorChannelId\" as a string")
            return true
        }

        try {
            guildId = config.get("guildId") as String
        } catch (_: Exception) {
            ChatOG.plugin.logger.severe("Failed to parse config option \"guildId\" as a string")
            return true
        }

        try {
            webhook = config.get("webhook") as String
        } catch (_: Exception) {
            ChatOG.plugin.logger.severe("Failed to parse config option \"webhook\" as a string")
            return true
        }

        try {
            staffWebhook = config.get("staffWebhook") as String
        } catch (_: Exception) {
            ChatOG.plugin.logger.severe("Failed to parse config option \"staffWebhook\" as a string")
            return true
        }

        try {
            donorWebhook = config.get("donorWebhook") as String
        } catch (_: Exception) {
            ChatOG.plugin.logger.severe("Failed to parse config option \"donorWebhook\" as a string")
            return true
        }

        try {
            listCommandName = config.get("listCommandName") as String
        } catch (_: Exception) {
            ChatOG.plugin.logger.severe("Failed to parse config option \"listCommandName\" as a string")
            return true
        }

        try {
            listCommandText = config.get("listCommandText") as String
        } catch (_: Exception) {
            ChatOG.plugin.logger.severe("Failed to parse config option \"listCommandText\" as a string")
            return true
        }

        colorCodeRoles = config.getStringList("colorCodeRoles")

        roles = config.getConfigurationSection("roles")?.getKeys(false) ?: setOf()

        if (roles.isEmpty()) {
            roleMessageColor = mutableMapOf()
            return false
        }

        val messageColors = mapOf<String, NamedTextColor>(
            Pair("BLACK", NamedTextColor.BLACK),
            Pair("DARK_BLUE", NamedTextColor.DARK_BLUE),
            Pair("DARK_GREEN", NamedTextColor.DARK_GREEN),
            Pair("DARK_AQUA", NamedTextColor.DARK_AQUA),
            Pair("DARK_RED", NamedTextColor.DARK_RED),
            Pair("DARK_PURPLE", NamedTextColor.DARK_PURPLE),
            Pair("GOLD", NamedTextColor.GOLD),
            Pair("GRAY", NamedTextColor.GRAY),
            Pair("DARK_GRAY", NamedTextColor.DARK_GRAY),
            Pair("BLUE", NamedTextColor.BLUE),
            Pair("GREEN", NamedTextColor.GREEN),
            Pair("AQUA", NamedTextColor.AQUA),
            Pair("RED", NamedTextColor.RED),
            Pair("LIGHT_PURPLE", NamedTextColor.LIGHT_PURPLE),
            Pair("YELLOW", NamedTextColor.YELLOW),
            Pair("WHITE", NamedTextColor.WHITE),
        )
        roles.forEach {
            val messageColorList = config.getStringList("roles.$it.message_color")
            roleMessageColor[it] = if (messageColorList.isEmpty()) {
                try {
                    config.get("roles.$it.message_color") as String
                } catch (_: Exception) {
                    return@forEach
                }

                if (!messageColors.contains(messageColorList[0].uppercase())) {
                    ChatOG.plugin.logger.severe("The message color for \"$it\" (\"${messageColorList[0]}\") is invalid.")
                    return@forEach
                }
                //roleMessageColor[it] = messageColors[messageColorList[0].uppercase()]!!
                messageColors[messageColorList[0].uppercase()]!!
            } else {
                if (messageColorList.size != 3) {
                    ChatOG.plugin.logger.severe("The message color for \"$it\" is not a color or an RGB value.")
                    return@forEach
                }

                messageColorList.forEach colorForEach@{ colorInList ->
                    try {
                        colorInList.toUByte()
                    } catch (_: Exception) {
                        ChatOG.plugin.logger.severe("The RGB value for \"$it\" is invalid.")
                        return@forEach
                    }
                }
                RGBColor(messageColorList[0].toInt(), messageColorList[1].toInt(), messageColorList[2].toInt())
            }
        }

        return false
    }
}