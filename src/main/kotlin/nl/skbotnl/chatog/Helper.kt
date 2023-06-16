package nl.skbotnl.chatog

import dev.minn.jda.ktx.coroutines.await
import org.bukkit.ChatColor
import java.util.*

object Helper {
    private var translateTimeout: MutableMap<UUID, Long> = HashMap()

    fun getTranslateTimeout(uuid: UUID): Long {
        val cooldown: Long
        if (!translateTimeout.containsKey(uuid)) {
            translateTimeout[uuid] = System.currentTimeMillis()
        }
        if (System.currentTimeMillis() - translateTimeout[uuid]!! > 1000) {
            translateTimeout[uuid] = System.currentTimeMillis()
            cooldown = 0
        } else {
            cooldown = System.currentTimeMillis() - translateTimeout[uuid]!!
        }
        return cooldown
    }
    fun convertColor(text: String): String {
        return ChatColor.translateAlternateColorCodes('&', text)
    }

    private val colorRegex = Regex("(§)?§([0-9a-fk-orA-FK-OR])")
    fun removeColor(text: String): String {
        var tempText = text
        colorRegex.findAll(text).iterator().forEach {
            tempText = tempText.replace(it.value, "")
        }
        return tempText
    }

    fun getColorSection(text: String): String {
        val it = colorRegex.findAll(text).iterator()

        var last = ""
        while (it.hasNext()) {
            last = it.next().value
        }

        return last
    }

    fun getFirstColorSection(text: String): String {
        val it = colorRegex.findAll(text).iterator()

        var first = ""
        while (it.hasNext()) {
            first = it.next().value
            break
        }

        return first
    }

    private val getColorRegex = Regex("(&)?&([0-9a-fk-orA-FK-OR])")
    fun getColor(text: String): String {
        val it = getColorRegex.findAll(text).iterator()

        var last = ""
        while (it.hasNext()) {
            last = it.next().value
        }

        return last
    }

    // 2 systems because Discord is still rolling out the new username system
    // TODO: Remove legacy username system
    private val getLegacyUsername = Regex("@(.*)#(\\d{4})")
    private val getHandle = Regex("@([a-z0-9_.]{2,32})")
    suspend fun convertMentions(text: String): String {
        val guild = DiscordBridge.jda?.getGuildById(DiscordBridge.guildId)
        if (guild == null) {
            ChatOG.plugin.logger.warning("Can't get the guild, is guildId set?")
            return text
        }

        var tempText = text
        val members = guild.loadMembers().await()

        val legacyIter = getLegacyUsername.findAll(text).iterator()
        legacyIter.forEach { legacy ->
            for (member in members) {
                if (member.user.name.lowercase() == legacy.groupValues[1].lowercase() && member.user.discriminator == legacy.groupValues[2]) {
                    tempText = tempText.replace(legacy.value, member.asMention)
                    return@forEach
                }
            }
        }

        val handleIter = getHandle.findAll(tempText).iterator()
        handleIter.forEach { handle ->
            for (member in members) {
                if (member.user.discriminator == "0000" && member.user.name.lowercase() == handle.groupValues[1].lowercase()) {
                    tempText = tempText.replace(handle.value, member.asMention)
                }
            }
        }

        return tempText
    }
}
