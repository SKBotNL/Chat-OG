package nl.skbotnl.chatog.commands

import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import nl.skbotnl.chatog.BingTranslator
import nl.skbotnl.chatog.ChatOG
import nl.skbotnl.chatog.Helper
import nl.skbotnl.chatog.Helper.convertColor
import nl.skbotnl.chatog.LanguageDatabase
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

class TranslateMessage : CommandExecutor {
    interface ISentMessage {
        val message: String
    }

    data class SentChatMessage(override val message: String, val player: Player) : ISentMessage
    data class SentCustomMessage(
        override val message: String,
        val username: String,
        val prefix: Component,
        val suffix: Component
    ) :
        ISentMessage

    companion object {
        val chatMessages: MutableMap<UUID, SentChatMessage> = HashMap()
        val customMessages: MutableMap<UUID, SentCustomMessage> = HashMap()
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender !is Player) {
            return false
        }
        if (args == null) {
            return false
        }
        if (args.isEmpty()) {
            return false
        }
        if (args.count() < 2) {
            return false
        }
        val player: Player = sender

        if (Helper.getTranslateTimeout(player.uniqueId) != 0L) {
            player.sendMessage(ChatOG.mm.deserialize("<red>You're doing that too fast"))
            return true
        }

        val uuid: UUID?

        try {
            uuid = UUID.fromString(args[0])
        } catch (e: IllegalArgumentException) {
            player.sendMessage(ChatOG.mm.deserialize("<red>That is not a valid UUID"))
            return true
        }

        val isCustomMessage = args[1].toBoolean()

        val sentMessage: ISentMessage? = if (isCustomMessage) {
            customMessages[uuid]
        } else {
            chatMessages[uuid]
        }

        if (sentMessage == null) {
            player.sendMessage(ChatOG.mm.deserialize("<red>Could not translate that message"))
            return true
        }

        val language = LanguageDatabase.getLanguage(player.uniqueId)

        if (language == "null") {
            player.sendMessage(ChatOG.mm.deserialize("<red>Please select the language to translate to first using /translatesettings"))
            return true
        }

        val translated = BingTranslator.translate(sentMessage.message, language)

        if (translated.error != null) {
            sender.sendMessage(translated.error)
            return true
        }

        if (translated.translatedText == null) {
            player.sendMessage(ChatOG.mm.deserialize("<red>Could not translate that message"))
            return true
        }

        val translateMessage: Component
        if (isCustomMessage) {
            val sentCustomMessage = sentMessage as SentCustomMessage

            val translatedComponent =
                Component.text("[${translated.translatedFrom} -> ${language}] ").color(NamedTextColor.LIGHT_PURPLE)

            val suffixTextComponent = sentCustomMessage.suffix as TextComponent
            val contentComponent =
                suffixTextComponent.content("${suffixTextComponent.content()}${translated.translatedText}")
            translateMessage = Component.join(
                JoinConfiguration.noSeparators(),
                translatedComponent,
                sentCustomMessage.prefix,
                contentComponent
            )
        } else {
            val sentChatMessage = sentMessage as SentChatMessage
            var chatString = "${ChatOG.chat.getPlayerPrefix(sentChatMessage.player)}${sentChatMessage.player.name}${
                ChatOG.chat.getPlayerSuffix(sentChatMessage.player)
            }"
            if (PlaceholderAPI.setPlaceholders(sentMessage.player, "%parties_party%") != "") {
                chatString = PlaceholderAPI.setPlaceholders(
                    sentMessage.player,
                    "&8[%parties_color_code%%parties_party%&8] $chatString"
                )
            }
            chatString = "&d[${translated.translatedFrom} -> ${language}] $chatString"
            chatString = convertColor(chatString)

            // Don't convert color in translated messages
            translateMessage = Component.text("$chatString${translated.translatedText}")
        }

        player.sendMessage(translateMessage)
        return true
    }
}