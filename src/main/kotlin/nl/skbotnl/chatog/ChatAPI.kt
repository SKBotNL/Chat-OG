package nl.skbotnl.chatog

import java.util.*
import kotlin.concurrent.read
import kotlinx.coroutines.launch
import nl.skbotnl.chatog.ChatOG.Companion.config
import nl.skbotnl.chatog.ChatOG.Companion.discordBridge
import nl.skbotnl.chatog.ChatOG.Companion.discordBridgeLock
import nl.skbotnl.chatog.ChatOG.Companion.scope
import org.bukkit.entity.Player

class ChatAPI {
    @Suppress("unused")
    fun isDiscordEnabled(): Boolean {
        return config.discordEnabled
    }

    @Suppress("unused")
    fun isStaffDiscordEnabled(): Boolean {
        return config.staffDiscordEnabled
    }

    @Suppress("unused")
    fun isPremiumDiscordEnabled(): Boolean {
        return config.premiumDiscordEnabled
    }

    @Suppress("unused")
    fun sendMessageWithBot(message: String) {
        if (!config.discordEnabled) return
        scope.launch {
            discordBridgeLock.read {
                val discordBridge = discordBridge
                discordBridge!!.sendMessageWithBot(message)
            }
        }
        return
    }

    fun sendMessage(message: String, player: Player) {
        if (!config.discordEnabled) return
        discordBridgeLock.read {
            val discordBridge = discordBridge
            scope.launch { discordBridge!!.sendMessage(message, player) }
        }
        return
    }

    fun sendMessage(message: String, name: String, uuid: UUID?) {
        if (!config.discordEnabled) return
        discordBridgeLock.read {
            val discordBridge = discordBridge
            scope.launch { discordBridge!!.sendMessage(message, name, uuid) }
        }
        return
    }

    @Suppress("unused")
    fun sendStaffMessage(message: String, name: String, uuid: UUID?) {
        if (!config.discordEnabled) return
        discordBridgeLock.read {
            val discordBridge = discordBridge
            scope.launch { discordBridge!!.sendStaffMessage(message, name, uuid) }
        }
        return
    }

    @Suppress("unused")
    fun sendPremiumMessage(message: String, name: String, uuid: UUID?) {
        if (!config.discordEnabled) return
        discordBridgeLock.read {
            val discordBridge = discordBridge
            scope.launch { discordBridge!!.sendPremiumMessage(message, name, uuid) }
        }
        return
    }

    @Suppress("unused")
    fun sendEmbed(message: String, uuid: UUID?, color: Int?) {
        if (!config.discordEnabled) return
        discordBridgeLock.read {
            val discordBridge = discordBridge
            scope.launch { discordBridge!!.sendEmbed(message, uuid, color) }
        }
        return
    }
}
