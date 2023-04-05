package nl.skbotnl.chatog

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import nl.skbotnl.chatog.Helper.convertColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

class TranslateMessage : CommandExecutor {
    data class SentMessage(val message: String, val username: String, val prefix: Component, val suffix: Component)

    companion object {
        val messages: MutableMap<UUID, SentMessage> = HashMap()
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
        val player: Player = sender

        if (Helper.getTranslateTimeout(player.uniqueId) != 0L) {
            player.sendMessage(convertColor("&cYou're doing that too fast"))
            return true
        }

        val uuid: UUID?

        try {
            uuid = UUID.fromString(args[0])
        }
        catch (e: IllegalArgumentException) {
            player.sendMessage(convertColor("&cThat is not a valid UUID"))
            return true
        }

        val sentMessage = messages[uuid]
        if (sentMessage == null) {
            player.sendMessage(convertColor("&cCould not translate that message"))
            return true
        }

        val language = LanguageDatabase.getLanguage(player.uniqueId)

        if (language == "null") {
            player.sendMessage(convertColor("&cPlease select the language to translate to first using /translatesettings"))
            return true
        }

        val translated = BingTranslator.translate(sentMessage.message, language)

        if (translated.error != null) {
            sender.sendMessage(translated.error)
            return true
        }

        if (translated.translatedText == null) {
            player.sendMessage(convertColor("&cCould not translate that message"))
            return true
        }

        val chatString = Component.join(JoinConfiguration.noSeparators(), Component.text(convertColor("&d[${translated.translatedFrom} -> ${language}] ")), sentMessage.prefix, Component.text(sentMessage.username), sentMessage.suffix, Component.text(translated.translatedText))

        player.sendMessage(chatString)
        return true
    }
}