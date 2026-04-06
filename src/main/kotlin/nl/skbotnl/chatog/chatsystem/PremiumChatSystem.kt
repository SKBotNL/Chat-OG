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

internal object PremiumChatSystem : ChatSystem() {
    override val prefix = "&aPREMIUM"
    override val audience: Audience
        get() = Audience.audience(Bukkit.getOnlinePlayers().filter { it.hasPermission("chat-og.premium") })

    override val name = "premium"

    override fun sendDiscordMessage(text: String, playerPartString: String, uuid: UUID) {
        if (config.premiumDiscordEnabled) {
            val discordMessageString = ChatUtil.convertEmojis(text)

            scope.launch {
                discordBridgeLock.read {
                    discordBridge?.sendPremiumMessage(discordMessageString, playerPartString, uuid)
                }
            }
        }
    }
}
