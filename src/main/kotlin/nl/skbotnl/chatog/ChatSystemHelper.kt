package nl.skbotnl.chatog

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.clip.placeholderapi.PlaceholderAPI
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.TextColor
import nl.skbotnl.chatog.commands.TranslateMessage
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

@OptIn(DelicateCoroutinesApi::class)
object ChatSystemHelper {
    object ChatType {
        const val STAFFCHAT = "staffchat"
        const val DONORCHAT = "donorchat"
    }

    var inChat: MutableMap<UUID, String> = HashMap()
    private val urlRegex = Regex("(.*)((https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])(.*)")

    fun sendMessageInStaffChat(player: Player, text: String) {
        var chatString = "${ChatOG.chat.getPlayerPrefix(player)}${player.name}"

        if (PlaceholderAPI.setPlaceholders(player, "%parties_party%") != "") {
            chatString = PlaceholderAPI.setPlaceholders(player, "&8[%parties_color_code%%parties_party%&8] $chatString")
        }
        chatString = "&cSTAFF | $chatString"
        val colorChatString = Helper.convertColor(chatString)

        var discordMessageString = ""
        if (DiscordBridge.jda != null) {
            discordMessageString = text
            var guildEmojis: List<RichCustomEmoji>? = null
            try {
                guildEmojis = DiscordBridge.jda?.getGuildById(DiscordBridge.guildId)?.emojis
            } catch (e: Exception) {
                ChatOG.plugin.logger.warning("Can't get the guild's emojis, is the guildId set?")
            }

            if (guildEmojis != null) {
                val regex = Regex(":(.*?):+")
                regex.findAll(text).iterator().forEach {
                    guildEmojis.forEach { emoji ->
                        if (emoji.name == it.groupValues[1]) {
                            val replaceWith = "<${if (emoji.isAnimated) "a" else ""}:${it.groupValues[1]}:${emoji.id}>"
                            discordMessageString = discordMessageString.replace(it.value, replaceWith)
                        }
                    }
                }
            }
        }

        val messageComponents = mutableListOf<Component>()

        text.split(" ").forEach { word ->
            val urlIter = urlRegex.findAll(word).iterator()
            val chatColor = Helper.getColor(ChatOG.chat.getPlayerSuffix(player))

            if (urlIter.hasNext()) {
                urlIter.forEach { link ->
                    if (BlocklistManager.checkUrl(word)) {
                        player.sendMessage(Helper.convertColor("&cWARNING: You are not allowed to post links like that here."))
                        for (p in Bukkit.getOnlinePlayers()) {
                            if (p.hasPermission("group.moderator")) {
                                p.sendMessage(
                                    Helper.convertColor(
                                        "[&aChat&f-&cOG&f]: ${p.name} has posted a disallowed link: ${
                                            word.replace(
                                                ".",
                                                "[dot]"
                                            )
                                        }."
                                    )
                                )
                            }
                        }
                        return
                    }

                    var linkComponent = Component.text(link.groups[2]!!.value).color(TextColor.color(34, 100, 255))
                    linkComponent = linkComponent.hoverEvent(
                        HoverEvent.hoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            Component.text(Helper.convertColor("&aClick to open link"))
                        )
                    )

                    linkComponent = linkComponent.clickEvent(
                        ClickEvent.clickEvent(
                            ClickEvent.Action.OPEN_URL,
                            link.groups[2]!!.value
                        )
                    )

                    val beforeComponent = Component.text(
                        Helper.convertColor(chatColor + (link.groups[1]?.value ?: ""))
                    )
                    val afterComponent = Component.text(
                        Helper.convertColor(chatColor + (link.groups[4]?.value ?: ""))
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
                    if (Helper.getColorSection(lastContent) != "" && Helper.getFirstColorSection(word) == "") {
                        Helper.convertColor(Helper.getColorSection(lastContent) + word)
                    } else {
                        Helper.convertColor(chatColor + word)
                    }
                } else {
                    Helper.convertColor(chatColor + word)
                }
            } else {
                Helper.convertColor(chatColor) + word
            }
            messageComponents += Component.text(wordText)
        }

        if (DiscordBridge.jda != null) {
            GlobalScope.launch {
                DiscordBridge.sendStaffMessage(discordMessageString, colorChatString, player.uniqueId)
            }
        }

        val messageComponent =
            Component.join(JoinConfiguration.separator(Component.text(" ")), messageComponents) as TextComponent

        chatString = Helper.convertColor("$colorChatString${ChatOG.chat.getPlayerSuffix(player)}")

        var textComponent =
            Component.join(JoinConfiguration.noSeparators(), Component.text(chatString), messageComponent)
        textComponent = textComponent.hoverEvent(
            HoverEvent.hoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                Component.text(Helper.convertColor("&aClick to translate this message"))
            )
        )

        val randomUUID = UUID.randomUUID()
        textComponent = textComponent.clickEvent(
            ClickEvent.clickEvent(
                ClickEvent.Action.RUN_COMMAND,
                "/translatemessage $randomUUID false"
            )
        )

        TranslateMessage.chatMessages[randomUUID] = TranslateMessage.SentChatMessage(text, player)

        for (p in Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("chat-og.staff")) {
                p.sendMessage(textComponent)
            }
        }
    }

    fun sendMessageInDonorChat(player: Player, text: String) {
        var chatString = "${ChatOG.chat.getPlayerPrefix(player)}${player.name}"

        if (PlaceholderAPI.setPlaceholders(player, "%parties_party%") != "") {
            chatString = PlaceholderAPI.setPlaceholders(player, "&8[%parties_color_code%%parties_party%&8] $chatString")
        }
        chatString = "&aDONOR | $chatString"
        val colorChatString = Helper.convertColor(chatString)

        var discordMessageString = ""
        if (DiscordBridge.jda != null) {
            discordMessageString = text
            var guildEmojis: List<RichCustomEmoji>? = null
            try {
                guildEmojis = DiscordBridge.jda?.getGuildById(DiscordBridge.guildId)?.emojis
            } catch (e: Exception) {
                ChatOG.plugin.logger.warning("Can't get the guild's emojis, is the guildId set?")
            }

            if (guildEmojis != null) {
                val regex = Regex(":(.*?):+")
                regex.findAll(text).iterator().forEach {
                    guildEmojis.forEach { emoji ->
                        if (emoji.name == it.groupValues[1]) {
                            val replaceWith = "<${if (emoji.isAnimated) "a" else ""}:${it.groupValues[1]}:${emoji.id}>"
                            discordMessageString = discordMessageString.replace(it.value, replaceWith)
                        }
                    }
                }
            }
        }

        val messageComponents = mutableListOf<Component>()

        text.split(" ").forEach { word ->
            val urlIter = urlRegex.findAll(word).iterator()
            val chatColor = Helper.getColor(ChatOG.chat.getPlayerSuffix(player))

            if (urlIter.hasNext()) {
                urlIter.forEach { link ->
                    if (BlocklistManager.checkUrl(word)) {
                        player.sendMessage(Helper.convertColor("&cWARNING: You are not allowed to post links like that here."))
                        for (p in Bukkit.getOnlinePlayers()) {
                            if (p.hasPermission("group.moderator")) {
                                p.sendMessage(
                                    Helper.convertColor(
                                        "[&aChat&f-&cOG&f]: ${p.name} has posted a disallowed link: ${
                                            word.replace(
                                                ".",
                                                "[dot]"
                                            )
                                        }."
                                    )
                                )
                            }
                        }
                        return
                    }

                    var linkComponent = Component.text(link.groups[2]!!.value).color(TextColor.color(34, 100, 255))
                    linkComponent = linkComponent.hoverEvent(
                        HoverEvent.hoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            Component.text(Helper.convertColor("&aClick to open link"))
                        )
                    )

                    linkComponent = linkComponent.clickEvent(
                        ClickEvent.clickEvent(
                            ClickEvent.Action.OPEN_URL,
                            link.groups[2]!!.value
                        )
                    )

                    val beforeComponent = Component.text(
                        Helper.convertColor(chatColor + (link.groups[1]?.value ?: ""))
                    )
                    val afterComponent = Component.text(
                        Helper.convertColor(chatColor + (link.groups[4]?.value ?: ""))
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
                    if (Helper.getColorSection(lastContent) != "" && Helper.getFirstColorSection(word) == "") {
                        Helper.convertColor(Helper.getColorSection(lastContent) + word)
                    } else {
                        Helper.convertColor(chatColor + word)
                    }
                } else {
                    Helper.convertColor(chatColor + word)
                }
            } else {
                Helper.convertColor(chatColor) + word
            }
            messageComponents += Component.text(wordText)
        }

        if (DiscordBridge.jda != null) {
            GlobalScope.launch {
                DiscordBridge.sendDonorMessage(discordMessageString, colorChatString, player.uniqueId)
            }
        }

        val messageComponent =
            Component.join(JoinConfiguration.separator(Component.text(" ")), messageComponents) as TextComponent

        chatString = Helper.convertColor("$colorChatString${ChatOG.chat.getPlayerSuffix(player)}")

        var textComponent =
            Component.join(JoinConfiguration.noSeparators(), Component.text(chatString), messageComponent)
        textComponent = textComponent.hoverEvent(
            HoverEvent.hoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                Component.text(Helper.convertColor("&aClick to translate this message"))
            )
        )

        val randomUUID = UUID.randomUUID()
        textComponent = textComponent.clickEvent(
            ClickEvent.clickEvent(
                ClickEvent.Action.RUN_COMMAND,
                "/translatemessage $randomUUID false"
            )
        )

        TranslateMessage.chatMessages[randomUUID] = TranslateMessage.SentChatMessage(text, player)

        for (p in Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("chat-og.donors")) {
                p.sendMessage(textComponent)
            }
        }
    }
}