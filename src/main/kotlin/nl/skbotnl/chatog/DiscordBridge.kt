package nl.skbotnl.chatog

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.execute
import dev.kord.core.entity.Role
import dev.kord.core.entity.Webhook
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import kotlinx.coroutines.flow.toList
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import nl.skbotnl.chatog.Helper.removeColor
import org.bukkit.Bukkit
import java.util.*

object DiscordBridge {
    private lateinit var webhook: Webhook
    suspend fun main() {
        val kord = Kord(Config.getBotToken())

        val webhookRegex = Regex(".*discord.com/api/webhooks/(.*)/(.*)")

        if (!webhookRegex.matches(Config.getWebhook())) {
            ChatOG.plugin.logger.severe("Invalid webhook")
            return
        }

        val webhookMatches = webhookRegex.find(Config.getWebhook())!!
        val (webhookId, webhookToken) = webhookMatches.destructured
        webhook = kord.getWebhookWithToken(Snowflake(webhookId), webhookToken)

        val channelId = Config.getChannelId()
        kord.on<MessageCreateEvent> {
            if (message.channelId != Snowflake(channelId)) {
                return@on
            }

            val author = message.author
            val roleList = author?.asMember(message.getGuild().id)?.roles?.toList() ?: return@on

            var highestRole: Role? = null
            if (roleList.isNotEmpty()) {
                highestRole = roleList[0]
                roleList.drop(1).forEach {
                    if (highestRole == null) {
                        highestRole = it
                        return@forEach
                    }
                    if (it.getPosition() > highestRole!!.getPosition()) {
                        highestRole = it
                    }
                }
            }


            val textColor: TextColor = if (highestRole == null) {
                TextColor.color(153, 170, 181)
            } else {
                TextColor.color(highestRole!!.color.rgb)
            }

            val discordComponent = Component.text("Discord: ").color(TextColor.color(88, 101, 242))
            val userComponent: TextComponent = if (highestRole == null) {
                Component.text(author.username).color(NamedTextColor.GRAY)
            } else {
                Component.text("[#${highestRole!!.name}] ${author.username}").color(textColor)
            }
            val contentComponent = Component.text(" > ${message.content}").color(NamedTextColor.GRAY)

            val message = Component.join(JoinConfiguration.noSeparators(), discordComponent, userComponent, contentComponent)
            Bukkit.broadcast(message)
        }

        kord.login {
            @OptIn(PrivilegedIntent::class)
            intents += Intent.MessageContent
        }
    }

    suspend fun sendMessage(message: String, player: String, uuid: UUID) {
        webhook.execute(webhook.token!!, null) {
            username = removeColor(player)
            avatarUrl = "https://crafatar.com/avatars/$uuid"
            content = message
        }
    }
}