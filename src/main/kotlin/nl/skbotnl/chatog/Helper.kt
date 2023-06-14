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
}