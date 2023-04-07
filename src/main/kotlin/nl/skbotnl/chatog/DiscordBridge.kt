package nl.skbotnl.chatog

import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.send.WebhookMessageBuilder
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.jdabuilder.intents
import dev.minn.jda.ktx.jdabuilder.light
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import nl.skbotnl.chatog.Helper.convertColor
import nl.skbotnl.chatog.Helper.removeColor
import org.bukkit.Bukkit
import java.util.*


object DiscordBridge {
    lateinit var jda: JDA
    private lateinit var webhook: WebhookClient
    var channelId = Config.getChannelId()

    fun main() {
        webhook = WebhookClient.withUrl(Config.getWebhook())

        jda = light(Config.getBotToken(), enableCoroutines=true) {
            intents += listOf(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT)
        }

        jda.listener<MessageReceivedEvent> {
            if (it.channel.id != channelId) {
                return@listener
            }

            val message = it.message

            val member = it.member ?: return@listener
            val color = member.color
            val textColor: TextColor = if (color == null) {
                TextColor.color(153, 170, 181)
            } else {
                TextColor.color(color.rgb)
            }

            val roles: List<Role> = member.roles
            val highestRole: Role = roles[roles.size - 1]

            val discordComponent = Component.text("Discord: ").color(TextColor.color(88, 101, 242))
            val userComponent: TextComponent = if (color == null) {
                Component.text(member.effectiveName).color(NamedTextColor.GRAY)
            } else {
                Component.text("[#${highestRole.name}] ${member.effectiveName}").color(textColor)
            }

            val configRoles = Config.getRoles()

            var messageColor: TextColor? = null
            if (configRoles == null) {
                messageColor = NamedTextColor.GRAY
            } else {
                for (roleId in configRoles) {
                    if (!member.roles.contains(message.guild.getRoleById(roleId))) {
                        continue
                    }

                    val roleColor = Config.getRoleMessageColor(roleId)
                    if (roleColor is List<*>) {
                        messageColor = TextColor.color(roleColor.elementAt(0).toString().toInt(), roleColor.elementAt(1).toString().toInt(), roleColor.elementAt(2).toString().toInt())
                        continue
                    }
                    if (roleColor !is String) {
                        continue
                    }
                    if (roleColor == "null") {
                        continue
                    }

                    messageColor = when (roleColor) {
                        "BLACK" -> NamedTextColor.BLACK
                        "DARK_BLUE" -> NamedTextColor.DARK_BLUE
                        "DARK_GREEN" -> NamedTextColor.DARK_GREEN
                        "DARK_AQUA" -> NamedTextColor.DARK_AQUA
                        "DARK_RED" -> NamedTextColor.DARK_RED
                        "DARK_PURPLE" -> NamedTextColor.DARK_PURPLE
                        "GOLD" -> NamedTextColor.GOLD
                        "GRAY" -> NamedTextColor.GRAY
                        "DARK_GRAY" -> NamedTextColor.DARK_GRAY
                        "BLUE" -> NamedTextColor.BLUE
                        "GREEN" -> NamedTextColor.GREEN
                        "AQUA" -> NamedTextColor.AQUA
                        "RED" -> NamedTextColor.RED
                        "LIGHT_PURPLE" -> NamedTextColor.LIGHT_PURPLE
                        "YELLOW" -> NamedTextColor.YELLOW
                        "WHITE" -> NamedTextColor.WHITE
                        else -> {
                            NamedTextColor.GRAY
                        }
                    }
                }
                if (messageColor == null) {
                    messageColor = NamedTextColor.GRAY
                }
            }

            val contentComponent = Component.text(" > ${message.contentDisplay}").color(messageColor)

            var messageComponent = Component.join(JoinConfiguration.noSeparators(), discordComponent, userComponent, contentComponent)
            messageComponent = messageComponent.hoverEvent(
                HoverEvent.hoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    Component.text(convertColor("&aClick to translate this message"))
                )
            )

            val randomUUID = UUID.randomUUID()
            messageComponent = messageComponent.clickEvent(
                ClickEvent.clickEvent(
                    ClickEvent.Action.RUN_COMMAND,
                    "/translatemessage $randomUUID true"
                )
            )

            Bukkit.broadcast(messageComponent)

            TranslateMessage.customMessages[randomUUID] = TranslateMessage.SentCustomMessage(message.contentDisplay, member.effectiveName, Component.join(JoinConfiguration.noSeparators(), discordComponent, userComponent), Component.text(" > ").color(messageColor))
        }
    }

    fun sendMessage(message: String, player: String, uuid: UUID) {
        val builder = WebhookMessageBuilder()
        builder.setUsername(removeColor(player))
        builder.setAvatarUrl("https://crafatar.com/avatars/$uuid")
        builder.setContent(message)

        webhook.send(builder.build())
    }
}