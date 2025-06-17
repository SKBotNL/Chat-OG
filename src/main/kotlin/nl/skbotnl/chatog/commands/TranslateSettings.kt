package nl.skbotnl.chatog.commands

import kotlinx.coroutines.DelicateCoroutinesApi
import net.trueog.utilitiesog.UtilitiesOG
import nl.skbotnl.chatog.ChatOG
import nl.skbotnl.chatog.Config
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class TranslateSettings : CommandExecutor {
    companion object {
        val languagesList =
            listOf(
                "ar",
                "az",
                "bg",
                "bn",
                "ca",
                "cs",
                "da",
                "de",
                "el",
                "en",
                "eo",
                "es",
                "et",
                "fa",
                "fi",
                "fr",
                "ga",
                "he",
                "hi",
                "hu",
                "id",
                "it",
                "ja",
                "ko",
                "lt",
                "lv",
                "ms",
                "nb",
                "nl",
                "pl",
                "pt",
                "ro",
                "ru",
                "sk",
                "sl",
                "sq",
                "sv",
                "th",
                "tl",
                "tr",
                "uk",
                "zh",
                "zt",
            )
    }

    @OptIn(DelicateCoroutinesApi::class)
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

        val language = args[0]

        if (languagesList.indexOf(language) == -1) {
            sender.sendMessage(UtilitiesOG.trueogColorize("${Config.prefix}<reset>: <red>Invalid language."))
            return true
        }

        ChatOG.languageDatabase.setPlayerLanguage(sender.uniqueId, language)
        sender.sendMessage(
            UtilitiesOG.trueogColorize("${Config.prefix}<reset>: <green>Set language to: <white>$language.")
        )

        return true
    }
}
