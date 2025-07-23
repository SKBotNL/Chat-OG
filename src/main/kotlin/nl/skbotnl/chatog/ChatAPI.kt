package nl.skbotnl.chatog

import java.util.UUID
import kotlin.concurrent.read
import kotlinx.coroutines.launch
import nl.skbotnl.chatog.ChatOG.Companion.discordBridgeLock
import nl.skbotnl.chatog.ChatOG.Companion.scope
import org.bukkit.entity.Player

class ChatAPI {
    @Suppress("unused")
    fun isDiscordEnabled(): Boolean {
        return ChatOG.config.discordEnabled
    }

    @Suppress("unused")
    fun isStaffDiscordEnabled(): Boolean {
        return ChatOG.config.staffDiscordEnabled
    }

    @Suppress("unused")
    fun isPremiumDiscordEnabled(): Boolean {
        return ChatOG.config.premiumDiscordEnabled
    }

    @Suppress("unused")
    fun sendMessageWithBot(message: String) {
        if (!ChatOG.config.discordEnabled) return
        scope.launch {
            discordBridgeLock.read {
                val discordBridge = ChatOG.discordBridge
                discordBridge!!.sendMessageWithBot(message)
            }
        }
        return
    }

    @Suppress("unused")
    fun sendMessage(message: String, player: Player) {
        if (!ChatOG.config.discordEnabled) return
        discordBridgeLock.read {
            val discordBridge = ChatOG.discordBridge
            scope.launch { discordBridge!!.sendMessage(message, player) }
        }
        return
    }

    @Suppress("unused")
    fun sendMessage(message: String, name: String, uuid: UUID?) {
        if (!ChatOG.config.discordEnabled) return
        discordBridgeLock.read {
            val discordBridge = ChatOG.discordBridge
            scope.launch { discordBridge!!.sendMessage(message, name, uuid) }
        }
        return
    }

    @Suppress("unused")
    fun sendStaffMessage(message: String, name: String, uuid: UUID?) {
        if (!ChatOG.config.discordEnabled) return
        discordBridgeLock.read {
            val discordBridge = ChatOG.discordBridge
            scope.launch { discordBridge!!.sendStaffMessage(message, name, uuid) }
        }
        return
    }

    @Suppress("unused")
    fun sendPremiumMessage(message: String, name: String, uuid: UUID?) {
        if (!ChatOG.config.discordEnabled) return
        discordBridgeLock.read {
            val discordBridge = ChatOG.discordBridge
            scope.launch { discordBridge!!.sendPremiumMessage(message, name, uuid) }
        }
        return
    }

    @Suppress("unused")
    fun sendEmbed(message: String, uuid: UUID?, color: Int?) {
        if (!ChatOG.config.discordEnabled) return
        discordBridgeLock.read {
            val discordBridge = ChatOG.discordBridge
            scope.launch { discordBridge!!.sendEmbed(message, uuid, color) }
        }
        return
    }
}
