package nl.skbotnl.chatog.commands

import nl.skbotnl.chatog.Helper.convertColor
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
            "af",
            "am",
            "ar",
            "as",
            "az",
            "ba",
            "bg",
            "bn",
            "bo",
            "bs",
            "ca",
            "cs",
            "cy",
            "da",
            "de",
            "dv",
            "el",
            "en",
            "es",
            "et",
            "eu",
            "fa",
            "fi",
            "fil",
            "fj",
            "fo",
            "fr",
            "fr-CA",
            "ga",
            "gl",
            "gu",
            "ha",
            "he",
            "hi",
            "hr",
            "hsb",
            "ht",
            "hu",
            "hy",
            "id",
            "ig",
            "ikt",
            "is",
            "it",
            "iu",
            "iu-Latn",
            "ja",
            "ka",
            "kk",
            "km",
            "kmr",
            "kn",
            "ko",
            "ku",
            "ky",
            "ln",
            "lo",
            "lt",
            "lug",
            "lv",
            "lzh",
            "mg",
            "mi",
            "mk",
            "ml",
            "mn-Cyrl",
            "mn-Mong",
            "mr",
            "ms",
            "mt",
            "mww",
            "my",
            "nb",
            "ne",
            "nl",
            "nso",
            "nya",
            "or",
            "otq",
            "pa",
            "pl",
            "prs",
            "ps",
            "pt",
            "pt-PT",
            "ro",
            "ru",
            "run",
            "rw",
            "sk",
            "sl",
            "sm",
            "sn",
            "so",
            "sq",
            "sr-Cyrl",
            "sr-Latn",
            "st",
            "sv",
            "sw",
            "ta",
            "te",
            "th",
            "ti",
            "tk",
            "tlh-Latn",
            "tlh-Piqd",
            "tn",
            "to",
            "tr",
            "tt",
            "ty",
            "ug",
            "uk",
            "ur",
            "uz",
            "vi",
            "xh",
            "yo",
            "yua",
            "yue",
            "zh-Hans",
            "zh-Hant",
            "zu"
        )

        if (languagesList.indexOf(language) == -1) {
            sender.sendMessage(convertColor("&cInvalid language"))
            return true
        }

        LanguageDatabase.setLanguage(player.uniqueId, language)
        player.sendMessage(convertColor("&aSet language to: $language"))

        return true
    }
}