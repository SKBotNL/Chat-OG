package nl.skbotnl.chatog.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

internal class TranslateSettingsTabCompleter : TabCompleter {
    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>?,
    ): MutableList<String> {
        return TranslateSettings.languagesList.toMutableList()
    }
}
