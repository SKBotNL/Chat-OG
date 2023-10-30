package nl.skbotnl.chatog.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class TranslateSettingsTabCompleter : TabCompleter {
    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>?
    ): MutableList<String> {
        return mutableListOf(
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
    }

}