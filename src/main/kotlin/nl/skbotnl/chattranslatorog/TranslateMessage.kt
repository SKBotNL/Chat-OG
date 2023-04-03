package nl.skbotnl.chattranslatorog

import net.kyori.adventure.text.TextComponent
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*
import kotlin.collections.HashMap

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

        val uuid: UUID?

        try {
            uuid = UUID.fromString(args[0])
        }
        catch (e: IllegalArgumentException) {
            player.sendMessage("§cThat is not a valid UUID")
            return true
        }

        val sentMessage = messages[uuid] ?: return true
        val message = sentMessage.message

        val language = LanguageDatabase.getLanguage(player.uniqueId)

        if (language == "null") {
            player.sendMessage("§cPlease select the language to translate to first using /translatesettings")
            return true
        }

        val translated = BingTranslator.translate(message.content(), language)

        if (translated.error != null) {
            sender.sendMessage(translated.error)
            return true
        }

        player.sendMessage("§d[${translated.translatedFrom} -> ${language}] §f<${sentMessage.player.name}> ${translated.translatedText}")
        return true
    }
}