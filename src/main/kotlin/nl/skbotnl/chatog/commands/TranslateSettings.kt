package nl.skbotnl.chatog.commands

import nl.skbotnl.chatog.ChatOG
import nl.skbotnl.chatog.LanguageDatabase
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class TranslateSettings : CommandExecutor {
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

        val language = args[0]

        val languagesList = listOf(
            "ar",
            "az",
            "ca",
            "zh",
            "cs",
            "da",
            "nl",
            "en",
            "eo",
            "fi",
            "fr",
            "de",
            "el",
            "he",
            "hi",
            "hu",
            "id",
            "ga",
            "it",
            "ja",
            "ko",
            "fa",
            "pl",
            "pt",
            "ru",
            "sk",
            "es",
            "sv",
            "tr",
            "uk"
        )

        if (languagesList.indexOf(language) == -1) {
            sender.sendMessage(ChatOG.mm.deserialize("<dark_gray>[<green>Chat<white>-<dark_red>OG<dark_gray>]<reset>: <red>Invalid language."))
            return true
        }

        LanguageDatabase.setLanguage(player.uniqueId, language)
        player.sendMessage(ChatOG.mm.deserialize("<dark_gray>[<green>Chat<white>-<dark_red>OG<dark_gray>]<reset>: <green>Set language to: <white>$language."))

        return true
    }
}