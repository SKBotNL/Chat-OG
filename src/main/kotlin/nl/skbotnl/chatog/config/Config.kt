package nl.skbotnl.chatog.config

import java.io.File
import nl.skbotnl.chatog.ChatOG.Companion.plugin
import tools.jackson.core.JacksonException
import tools.jackson.databind.exc.InvalidFormatException
import tools.jackson.databind.exc.ValueInstantiationException
import tools.jackson.dataformat.yaml.YAMLMapper

object Config {
    val configMapper = YAMLMapper()

    fun loadConfig(): ConfigModel? {
        return try {
            configMapper.readValue(File(plugin.dataFolder, "config.yml"), ConfigModel::class.java)
        } catch (e: ValueInstantiationException) {
            if (e.cause !is NullPointerException) throw e
            // Hacky but seems to be the only way
            plugin.logger.severe(
                "Failed to parse value for field \"${e.originalMessage?.substringAfterLast(" ") ?: "unknown"}\""
            )
            null
        } catch (e: InvalidFormatException) {
            plugin.logger.severe(
                "Invalid type for field \"${e.path.joinToString(".") { it.propertyName }}\":\n${e.originalMessage}"
            )
            null
        } catch (e: JacksonException) {
            plugin.logger.severe("Something went wrong while loading the config:\n${e.message}")
            null
        }
    }
}
