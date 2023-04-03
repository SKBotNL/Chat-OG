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

    private val bingTranslator = BingTranslator(Config.getApiKey())

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender !is Player) {
            return false
        }
        if (args == null) {
            return false
        }
        val player: Player = sender

        val uuid = UUID.fromString(args[0])

        val sentMessage = messages[uuid] ?: return true
        val message = sentMessage.message

        val language = LanguageDatabase.getLanguage(player.uniqueId)

        if (language == "null") {
            player.sendMessage("§cPlease select the language to translate to first using /translatesettings")
            return true
        }

        val translated = bingTranslator.translate(message.content(), language)

        if (translated.error != null) {
            sender.sendMessage(translated.error)
            return true
        }

        player.sendMessage("§d[${translated.translatedFrom} -> ${language}] §f<${sentMessage.player.name}> ${translated.translatedText}")
        return true
    }
}