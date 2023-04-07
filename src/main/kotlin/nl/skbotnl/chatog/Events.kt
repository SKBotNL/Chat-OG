package nl.skbotnl.chatog

import io.papermc.paper.advancement.AdvancementDisplay.Frame.*
import io.papermc.paper.event.player.AsyncChatEvent
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.translation.GlobalTranslator
import nl.skbotnl.chatog.Helper.convertColor
import nl.skbotnl.chatog.Helper.removeColor
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.server.BroadcastMessageEvent
import java.util.*

class Events : Listener {
    private var lastMessaged: MutableMap<UUID, UUID> = HashMap()

    @OptIn(DelicateCoroutinesApi::class)
    @EventHandler
    fun joinEvent(event: PlayerJoinEvent) {
        var discordString = "${ChatOG.chat.getPlayerPrefix(event.player)}${event.player.name}"

        if (PlaceholderAPI.setPlaceholders(event.player, "%parties_party%") != "") {
            discordString = PlaceholderAPI.setPlaceholders(event.player, "&8[%parties_color_code%%parties_party%&8] $discordString")
        }
        discordString = convertColor(discordString)
        GlobalScope.launch {
            DiscordBridge.sendEmbed("${removeColor(discordString)} joined the game. ${Bukkit.getOnlinePlayers().count()} player(s) online.", event.player.uniqueId, 0x00FF00)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    @EventHandler
    fun quitEvent(event: PlayerQuitEvent) {
        var discordString = "${ChatOG.chat.getPlayerPrefix(event.player)}${event.player.name}"

        if (PlaceholderAPI.setPlaceholders(event.player, "%parties_party%") != "") {
            discordString = PlaceholderAPI.setPlaceholders(event.player, "&8[%parties_color_code%%parties_party%&8] $discordString")
        }
        discordString = convertColor(discordString)
        GlobalScope.launch {
            DiscordBridge.sendEmbed("${removeColor(discordString)} left the game. ${Bukkit.getOnlinePlayers().count() - 1} player(s) online.", event.player.uniqueId, 0xFF0000)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    @EventHandler
    fun advancementEvent(event: PlayerAdvancementDoneEvent) {
        var discordString = "${ChatOG.chat.getPlayerPrefix(event.player)}${event.player.name}"

        if (PlaceholderAPI.setPlaceholders(event.player, "%parties_party%") != "") {
            discordString = PlaceholderAPI.setPlaceholders(event.player, "&8[%parties_color_code%%parties_party%&8] $discordString")
        }
        discordString = convertColor(discordString)

        val advancementKey = (event.advancement.display?.title() as TranslatableComponent).key()
        val translatedAdvancement = GlobalTranslator.translator().translate(advancementKey, Locale.US)
        Bukkit.broadcastMessage(translatedAdvancement.toString())
        val advancementMessage = when (event.advancement.display?.frame()) {
            GOAL -> "has reached the goal [$translatedAdvancement]"
            TASK -> "has made the advancement [$translatedAdvancement]"
            CHALLENGE -> "has completed the challenge [$translatedAdvancement]"
            else -> {
                return
            }
        }

        GlobalScope.launch {
            DiscordBridge.sendEmbed("${removeColor(discordString)} $advancementMessage.", event.player.uniqueId, 0xffff00)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    @EventHandler
    fun broadcastEvent(event: BroadcastMessageEvent) {
        GlobalScope.launch {
            DiscordBridge.sendMessage((event.message() as TextComponent).content(), "[Server] Broadcast", null)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    @EventHandler
    fun chatEvent(event: AsyncChatEvent) {
        event.isCancelled = true

        val oldTextComponent = event.message() as TextComponent

        var chatString = "${ChatOG.chat.getPlayerPrefix(event.player)}${event.player.name}"

        if (PlaceholderAPI.setPlaceholders(event.player, "%parties_party%") != "") {
            chatString = PlaceholderAPI.setPlaceholders(event.player, "&8[%parties_color_code%%parties_party%&8] $chatString")
        }
        val discordString = convertColor(chatString)

        GlobalScope.launch {
            DiscordBridge.sendMessage(oldTextComponent.content(), discordString, event.player.uniqueId)
        }

        chatString = "$discordString${ChatOG.chat.getPlayerSuffix(event.player)}${oldTextComponent.content()}"

        if (event.player.hasPermission("chat-og.color")) {
            chatString = convertColor(chatString)
        }

        var textComponent = Component.text(chatString)
        textComponent = textComponent.hoverEvent(
            HoverEvent.hoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                Component.text(convertColor("&aClick to translate this message"))
            )
        )

        val randomUUID = UUID.randomUUID()
        textComponent = textComponent.clickEvent(
            ClickEvent.clickEvent(
                ClickEvent.Action.RUN_COMMAND,
                "/translatemessage $randomUUID false"
            )
        )

        event.viewers().forEach {
            it.sendMessage(textComponent)
        }

        TranslateMessage.chatMessages[randomUUID] = TranslateMessage.SentChatMessage(oldTextComponent.content(), event.player)
    }

    @EventHandler
    fun commandPreprocess(event: PlayerCommandPreprocessEvent) {
        val checkSplit = event.message.split(" ", ignoreCase = true, limit = 2)[0]

        if (!(checkSplit == "/msg" || checkSplit == "/whisper" || checkSplit == "/pm" || checkSplit == "/reply" || checkSplit == "/r")) {
            return
        }
        event.isCancelled = true

        val messageSplit: List<String> = if (checkSplit == "/r" || checkSplit == "/reply") {
            event.message.split(" ", ignoreCase = true, limit = 2)
        } else {
            event.message.split(" ", ignoreCase = true, limit = 3)
        }

        if (messageSplit.count() < 3 && !(checkSplit == "/r" || checkSplit == "/reply")) {
            event.player.sendMessage(convertColor("&c${messageSplit[0]} <to> <message>"))
            return
        }
        if (messageSplit.count() < 2) {
            event.player.sendMessage(convertColor("&c${messageSplit[0]} <message>"))
            return
        }

        val player: Player?
        var message: String

        if (checkSplit == "/r" || checkSplit == "/reply") {
            message = messageSplit[1]

            if (!lastMessaged.containsKey(event.player.uniqueId)) {
                event.player.sendMessage(convertColor("&cYou haven't messaged anyone yet"))
                return
            }

            val lastMessagedPlayer = lastMessaged[event.player.uniqueId]
            player = Bukkit.getPlayer(lastMessagedPlayer!!)
        } else {
            player = Bukkit.getPlayer(messageSplit[1])
            message = messageSplit[2]
        }

        if (player == null) {
            event.player.sendMessage(convertColor("&cThat player doesn't exist or isn't online"))
            return
        }

        var textComponent = Component.text()
        textComponent = textComponent.hoverEvent(
            HoverEvent.hoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                Component.text(convertColor("&aClick to translate this message"))
            )
        )

        val randomUUID = UUID.randomUUID()
        textComponent = textComponent.clickEvent(
            ClickEvent.clickEvent(
                ClickEvent.Action.RUN_COMMAND,
                "/translatemessage $randomUUID false"
            )
        )

        TranslateMessage.customMessages[randomUUID] = TranslateMessage.SentCustomMessage(message, event.player.name, Component.text(convertColor("&6[PM]&4 ")), Component.text(" > ").color(NamedTextColor.GRAY))

        if (event.player.hasPermission("chat-og.color")) {
            message = convertColor(message)
        }

        var toSenderPrefix = "&6[&cme &6-> &4${player.name}&6]&f"
        toSenderPrefix = convertColor(toSenderPrefix)
        textComponent.content("$toSenderPrefix $message")
        event.player.sendMessage(textComponent)

        var toPrefix = "&6[&4${event.player.name} &6-> &cme&6]&f"
        toPrefix = convertColor(toPrefix)
        textComponent.content("$toPrefix $message")
        player.sendMessage(textComponent)

        lastMessaged[event.player.uniqueId] = player.uniqueId
        lastMessaged[player.uniqueId] = event.player.uniqueId

        return
    }

    @OptIn(DelicateCoroutinesApi::class)
    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        var nameString = "${ChatOG.chat.getPlayerPrefix(event.player)}${event.player.name}"

        if (PlaceholderAPI.setPlaceholders(event.player, "%parties_party%") != "") {
            nameString = PlaceholderAPI.setPlaceholders(event.player, "&8[%parties_color_code%%parties_party%&8] $nameString")
        }
        nameString = convertColor(nameString)

        var oldDeathMessage = event.deathMessage() as TranslatableComponent
        oldDeathMessage = oldDeathMessage.color(TextColor.color(16755200))
        oldDeathMessage = oldDeathMessage.append(Component.text("."))

        val argList = oldDeathMessage.args().toMutableList()
        argList[0] = Component.text(nameString)
        val deathMessage = oldDeathMessage.args(argList)

        event.deathMessage(deathMessage)

        GlobalScope.launch {
            val translatedDeathMessage = GlobalTranslator.translator().translate(oldDeathMessage.key(), Locale.US)
            DiscordBridge.sendEmbed("${removeColor(nameString)} was $translatedDeathMessage.", event.player.uniqueId, 0xFF0000)
        }
    }
}