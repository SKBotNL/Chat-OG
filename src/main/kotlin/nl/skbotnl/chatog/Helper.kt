package nl.skbotnl.chatog

import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
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
    private fun getColor(text: String): String {
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

    private val urlRegex =
        Regex("(.*)((https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;()]*[-a-zA-Z0-9+&@#/%=~_|()])(.*)")

    fun convertLinks(text: String, player: Player): MutableList<Component> {
        val messageComponents = mutableListOf<Component>()

        text.split(" ").forEach { word ->
            val urlIter = urlRegex.findAll(word).iterator()
            val chatColor = getColor(ChatOG.chat.getPlayerSuffix(player))

            if (urlIter.hasNext()) {
                urlIter.forEach { link ->
                    if (BlocklistManager.checkUrl(word)) {
                        player.sendMessage(convertColor("&cWARNING: You are not allowed to post links like that here."))
                        for (onlinePlayer in Bukkit.getOnlinePlayers()) {
                            if (onlinePlayer.hasPermission("group.moderator")) {
                                onlinePlayer.sendMessage(
                                    convertColor(
                                        "[&aChat&f-&cOG&f]: ${player.name} has posted a disallowed link: ${
                                            word.replace(
                                                ".",
                                                "[dot]"
                                            )
                                        }."
                                    )
                                )
                            }
                        }
                        return messageComponents
                    }

                    var linkComponent = Component.text(link.groups[2]!!.value).color(TextColor.color(34, 100, 255))
                    linkComponent = linkComponent.hoverEvent(
                        HoverEvent.hoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            Component.text(convertColor("&aClick to open link"))
                        )
                    )

                    linkComponent = linkComponent.clickEvent(
                        ClickEvent.clickEvent(
                            ClickEvent.Action.OPEN_URL,
                            link.groups[2]!!.value
                        )
                    )

                    val beforeComponent = Component.text(
                        convertColor(chatColor + (link.groups[1]?.value ?: ""))
                    )
                    val afterComponent = Component.text(
                        convertColor(chatColor + (link.groups[4]?.value ?: ""))
                    )

                    val fullComponent =
                        Component.join(JoinConfiguration.noSeparators(), beforeComponent, linkComponent, afterComponent)

                    messageComponents += fullComponent
                }
                return@forEach
            }
            val wordText = if (player.hasPermission("chat-og.color")) {
                if (messageComponents.isNotEmpty()) {
                    val lastContent = (messageComponents.last() as TextComponent).content()
                    if (getColorSection(lastContent) != "" && getFirstColorSection(word) == "") {
                        convertColor(getColorSection(lastContent) + word)
                    } else {
                        convertColor(chatColor + word)
                    }
                } else {
                    convertColor(chatColor + word)
                }
            } else {
                convertColor(chatColor) + word
            }
            messageComponents += Component.text(wordText)
        }

        return messageComponents
    }

    private val emojiRegex = Regex(":(.*?):+")

    fun convertEmojis(text: String): String {
        var discordMessageString = text
        var guildEmojis: List<RichCustomEmoji>? = null

        try {
            guildEmojis = DiscordBridge.jda?.getGuildById(DiscordBridge.guildId)?.emojis
        } catch (e: Exception) {
            ChatOG.plugin.logger.warning("Can't get the guild's emojis, is the guildId set?")
        }

        if (guildEmojis != null) {

            emojiRegex.findAll(text).iterator().forEach {
                guildEmojis.forEach { emoji ->
                    if (emoji.name == it.groupValues[1]) {
                        val replaceWith = "<${if (emoji.isAnimated) "a" else ""}:${it.groupValues[1]}:${emoji.id}>"
                        discordMessageString = discordMessageString.replace(it.value, replaceWith)
                    }
                }
            }
        }

        return discordMessageString
    }

    fun stripGroupMentions(text: String): String {
        var tempText = text.replace("@everyone", "@\u200Eeveryone", false)
        tempText = tempText.replace("@here", "@\u200Ehere", false)

        tempText = tempText.replace("(<@&)(\\d*>)".toRegex(), "$1\u200E$2")

        return tempText
    }
}
