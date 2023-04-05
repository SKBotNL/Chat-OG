package nl.skbotnl.chatog

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
}