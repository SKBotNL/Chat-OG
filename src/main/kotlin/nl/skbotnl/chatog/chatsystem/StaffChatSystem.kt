package nl.skbotnl.chatog.chatsystem

import java.util.UUID
import kotlin.concurrent.read
import kotlinx.coroutines.launch
import net.kyori.adventure.audience.Audience
import nl.skbotnl.chatog.ChatOG
import nl.skbotnl.chatog.ChatOG.Companion.config
import nl.skbotnl.chatog.ChatOG.Companion.discordBridgeLock
import nl.skbotnl.chatog.ChatSystem
import nl.skbotnl.chatog.ChatUtil
import org.bukkit.Bukkit

internal object StaffChatSystem : ChatSystem() {
    override val prefix = "&cSTAFF"
    override val audience: Audience
        get() = Audience.audience(Bukkit.getOnlinePlayers().filter { it.hasPermission("chat-og.staff") })

    override val name = "staff"

    override fun sendDiscordMessage(text: String, playerPartString: String, uuid: UUID) {
        if (config.staffDiscordEnabled) {
            val discordMessageString = ChatUtil.convertEmojis(text)

            ChatOG.scope.launch {
                discordBridgeLock.read {
                    ChatOG.discordBridge?.sendStaffMessage(discordMessageString, playerPartString, uuid)
                }
            }
        }
    }
}
