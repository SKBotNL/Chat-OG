package nl.skbotnl.chatog

import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.text.TextComponent
import nl.skbotnl.chatog.Helper.convertColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

class TranslateMessage : CommandExecutor {
    data class SentMessage(val message: TextComponent, val player: Player)

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

        val sentMessage = messages[uuid] ?: return true
        val message = sentMessage.message

        val language = LanguageDatabase.getLanguage(player.uniqueId)

        if (language == "null") {
            player.sendMessage(convertColor("&cPlease select the language to translate to first using /translatesettings"))
            return true
        }

        val translated = BingTranslator.translate(message.content(), language)

        if (translated.error != null) {
            sender.sendMessage(translated.error)
            return true
        }

        var chatString = "${ChatOG.chat.getPlayerPrefix(sentMessage.player)}${sentMessage.player.name}${ChatOG.chat.getPlayerSuffix(sentMessage.player)}"
        if (PlaceholderAPI.setPlaceholders(sentMessage.player, "%parties_party%") != "") {
            chatString = PlaceholderAPI.setPlaceholders(sentMessage.player, "&8[%parties_color_code%%parties_party%&8] $chatString")
        }
        chatString = "&d[${translated.translatedFrom} -> ${language}] $chatString"
        chatString = convertColor(chatString)

        // Don't convert color in translated messages
        chatString = "$chatString${translated.translatedText}"

        player.sendMessage(chatString)
        return true
    }
}