package nl.skbotnl.chatog.chatsystem

import java.util.*
import kotlin.concurrent.read
import kotlinx.coroutines.launch
import net.kyori.adventure.audience.Audience
import nl.skbotnl.chatog.ChatOG.Companion.config
import nl.skbotnl.chatog.ChatOG.Companion.discordBridge
import nl.skbotnl.chatog.ChatOG.Companion.discordBridgeLock
import nl.skbotnl.chatog.ChatOG.Companion.scope
import nl.skbotnl.chatog.util.ChatUtil
import org.bukkit.Bukkit

internal object DeveloperChatSystem : ChatSystem() {
    override val prefix = "<aqua>DEVELOPER"
    override val audience: Audience
        get() = Audience.audience(Bukkit.getOnlinePlayers().filter { it.hasPermission("chat-og.developer") })

    override val name = "developer"

    override fun sendDiscordMessage(text: String, playerPartString: String, uuid: UUID) {
        if (config.developerDiscordEnabled) {
            val discordMessageString = ChatUtil.convertEmojis(text)

            scope.launch {
                discordBridgeLock.read {
                    discordBridge?.sendDeveloperMessage(discordMessageString, playerPartString, uuid)
                }
            }
        }
    }
}
