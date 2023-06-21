package nl.skbotnl.chatog

import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.send.WebhookEmbed
import club.minnced.discord.webhook.send.WebhookEmbedBuilder
import club.minnced.discord.webhook.send.WebhookMessageBuilder
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.generics.getChannel
import dev.minn.jda.ktx.jdabuilder.cache
import dev.minn.jda.ktx.jdabuilder.intents
import dev.minn.jda.ktx.jdabuilder.light
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import nl.skbotnl.chatog.Helper.convertColor
import nl.skbotnl.chatog.Helper.convertMentions
import nl.skbotnl.chatog.Helper.removeColor
import nl.skbotnl.chatog.commands.TranslateMessage
import org.bukkit.Bukkit
import java.util.*

object DiscordBridge {
    var jda: JDA? = null
    var webhook: WebhookClient? = null
    var staffWebhook: WebhookClient? = null
    var donorWebhook: WebhookClient? = null
    var guildId = Config.getGuildId()
    var channelId = Config.getChannelId()
    var staffChannelId = Config.getStaffChannelId()
    var donorChannelId = Config.getDonorChannelId()

    private val urlRegex = Regex("(.*)((https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])(.*)")

    fun main() {
        try {
            webhook = WebhookClient.withUrl(Config.getWebhook())
        } catch (e: IllegalArgumentException) {
            ChatOG.plugin.logger.warning("webhook has not been set or is invalid")
        }
        try {
            staffWebhook = WebhookClient.withUrl(Config.getStaffWebhook())
        } catch (e: IllegalArgumentException) {
            ChatOG.plugin.logger.warning("staffWebhook has not been set or is invalid")
        }
        try {
            donorWebhook = WebhookClient.withUrl(Config.getDonorWebhook())
        } catch (e: IllegalArgumentException) {
            ChatOG.plugin.logger.warning("donorWebhook has not been set or is invalid")
        }

        jda = light(Config.getBotToken(), enableCoroutines = true) {
            intents += listOf(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT)
            cache += listOf(CacheFlag.EMOJI)
            setMemberCachePolicy(MemberCachePolicy.ALL)
        }

        jda?.presence?.setPresence(Activity.playing(Config.getStatus()), false)

        jda?.listener<ReadyEvent> {
            sendMessageWithBot("The server has started <:stonks:899680228216029195>")
            jda?.getGuildById(guildId)?.upsertCommand("list", "List all online players.")?.queue()
        }

        jda?.listener<SlashCommandInteractionEvent> {
            if (it.name == "list") {
                it.deferReply().queue()
                try {
                    if (Bukkit.getOnlinePlayers().isEmpty()) {
                        it.reply("There are no players online.").queue()
                        return@listener
                    }
                    it.reply(
                        "${Bukkit.getOnlinePlayers().count()} player(s) online:\n${
                            Bukkit.getOnlinePlayers().joinToString(separator = ", ") { player -> player.name }
                        }"
                    ).queue()
                } catch (e: Exception) {
                    ChatOG.plugin.logger.warning("Could not respond to /list interaction")
                }
            }
        }

        jda?.listener<MessageReceivedEvent> {
            if (it.channel.id != channelId && it.channel.id != staffChannelId && it.channel.id != donorChannelId) {
                return@listener
            }
            if (it.author.isBot) {
                return@listener
            }

            val message = it.message

            val user = it.author
            val member = it.member ?: return@listener
            val color = member.color
            val textColor: TextColor = if (color == null) {
                TextColor.color(153, 170, 181)
            } else {
                TextColor.color(color.rgb)
            }

            val roles = member.roles
            val roleIds = roles.map { role -> role.id }
            val topRole = roles.maxBy { role -> role.positionRaw }

            val discordComponent = Component.text("Discord: ").color(TextColor.color(88, 101, 242))
            val userComponent: TextComponent = if (color == null) {
                Component.text(member.effectiveName).color(NamedTextColor.GRAY)
            } else {
                Component.text("[#${topRole.name}] ${member.effectiveName}").color(textColor)
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
                        messageColor = TextColor.color(
                            roleColor.elementAt(0).toString().toInt(),
                            roleColor.elementAt(1).toString().toInt(),
                            roleColor.elementAt(2).toString().toInt()
                        )
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

            val messageComponents = mutableListOf<Component>(Component.text(" >").color(messageColor))

            val attachmentComponents = mutableListOf<Component>()

            if (message.attachments.isNotEmpty()) {
                message.attachments.forEach { attachment ->
                    var linkComponent = Component.text(attachment.url).color(TextColor.color(34, 100, 255))

                    linkComponent = linkComponent.hoverEvent(
                        HoverEvent.hoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            Component.text(convertColor("&aClick to open link"))
                        )
                    )

                    linkComponent = linkComponent.clickEvent(
                        ClickEvent.clickEvent(
                            ClickEvent.Action.OPEN_URL,
                            attachment.url
                        )
                    )

                    attachmentComponents += linkComponent
                }
            }

            for (word in message.contentDisplay.split(" ")) {
                val urlIter = urlRegex.findAll(word).iterator()
                if (urlIter.hasNext()) {
                    for (url in urlIter) {
                        if (BlocklistManager.checkUrl(word)) {
                            for (player in Bukkit.getOnlinePlayers()) {
                                if (player.hasPermission("group.moderator")) {
                                    player.sendMessage(
                                        convertColor(
                                            "[&aChat&f-&cOG&f]: @${user.name} has posted a disallowed link: ${
                                                word.replace(
                                                    ".",
                                                    "[dot]"
                                                )
                                            }."
                                        )
                                    )
                                }
                            }
                            return@listener
                        }

                        var linkComponent = Component.text(url.groups[2]!!.value).color(TextColor.color(34, 100, 255))
                        linkComponent = linkComponent.hoverEvent(
                            HoverEvent.hoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                Component.text(convertColor("&aClick to open link"))
                            )
                        )

                        linkComponent = linkComponent.clickEvent(
                            ClickEvent.clickEvent(
                                ClickEvent.Action.OPEN_URL,
                                url.groups[2]!!.value
                            )
                        )

                        val beforeComponent = Component.text(url.groups[1]?.value ?: "").color(messageColor)
                        val afterComponent = Component.text(url.groups[4]?.value ?: "").color(messageColor)

                        val fullComponent = Component.join(
                            JoinConfiguration.noSeparators(),
                            beforeComponent,
                            linkComponent,
                            afterComponent
                        )
                        messageComponents += fullComponent
                    }
                    continue
                }

                var messageText = word
                if (Config.getColorCodeRoles().any { colorCodeRole -> colorCodeRole in roleIds }) {
                    messageText = convertColor(messageText)
                }
                messageComponents += Component.text(messageText).color(messageColor)
            }

            val contentComponent = Component.join(
                JoinConfiguration.separator(Component.text(" ")),
                messageComponents + attachmentComponents
            )

            var replyComponent = Component.text("")

            if (message.messageReference != null) {
                val replyMessage = message.messageReference!!.message
                if (replyMessage != null) {
                    replyComponent =
                        Component.text("[Reply to: ${replyMessage.author.effectiveName}] ").color(NamedTextColor.GREEN)
                }
            }

            var messageComponent: Component
            if (it.channel.id == staffChannelId) {
                val staffComponent = Component.text("STAFF | ").color(NamedTextColor.RED)
                messageComponent = Component.join(
                    JoinConfiguration.noSeparators(),
                    discordComponent,
                    staffComponent,
                    replyComponent,
                    userComponent,
                    contentComponent
                )
            } else if (it.channel.id == donorChannelId) {
                val donorComponent = Component.text("DONOR | ").color(NamedTextColor.GREEN)
                messageComponent = Component.join(
                    JoinConfiguration.noSeparators(),
                    discordComponent,
                    donorComponent,
                    replyComponent,
                    userComponent,
                    contentComponent
                )
            } else {
                messageComponent = Component.join(
                    JoinConfiguration.noSeparators(),
                    discordComponent,
                    replyComponent,
                    userComponent,
                    contentComponent
                )
            }

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

            if (it.channel.id == staffChannelId) {
                for (p in Bukkit.getOnlinePlayers()) {
                    if (p.hasPermission("chat-og.staff")) {
                        p.sendMessage(messageComponent)
                    }
                }
            } else if (it.channel.id == donorChannelId) {
                for (p in Bukkit.getOnlinePlayers()) {
                    if (p.hasPermission("chat-og.donors")) {
                        p.sendMessage(messageComponent)
                    }
                }
            } else {
                Bukkit.broadcast(messageComponent)
            }

            TranslateMessage.customMessages[randomUUID] = TranslateMessage.SentCustomMessage(
                message.contentDisplay,
                member.effectiveName,
                Component.join(JoinConfiguration.noSeparators(), discordComponent, userComponent),
                Component.text(" > ").color(messageColor)
            )
        }
    }

    fun sendMessageWithBot(message: String) {
        val channel = jda?.getChannel<MessageChannel>(channelId)
        if (channel == null) {
            ChatOG.plugin.logger.warning("channelId has not been set or is invalid")
        }

        channel?.sendMessage(message)?.complete()
    }

    suspend fun sendMessage(message: String, player: String, uuid: UUID?) {
        if (webhook == null) {
            ChatOG.plugin.logger.warning("webhook has not been set or is invalid")
            return
        }

        val webhookMessage = WebhookMessageBuilder()
            .setUsername(removeColor(player))
            .setContent(convertMentions(message))
        if (uuid != null) {
            webhookMessage.setAvatarUrl("https://crafatar.com/avatars/$uuid")
        }

        webhook?.send(webhookMessage.build())
    }

    suspend fun sendStaffMessage(message: String, player: String, uuid: UUID?) {
        if (staffWebhook == null) {
            ChatOG.plugin.logger.warning("staffWebhook has not been set or is invalid")
            return
        }

        val webhookMessage = WebhookMessageBuilder()
            .setUsername(removeColor(player))
            .setContent(convertMentions(message))
        if (uuid != null) {
            webhookMessage.setAvatarUrl("https://crafatar.com/avatars/$uuid")
        }

        staffWebhook?.send(webhookMessage.build())
    }

    suspend fun sendDonorMessage(message: String, player: String, uuid: UUID?) {
        if (donorWebhook == null) {
            ChatOG.plugin.logger.warning("donorWebhook has not been set or is invalid")
            return
        }

        val webhookMessage = WebhookMessageBuilder()
            .setUsername(removeColor(player))
            .setContent(convertMentions(message))
        if (uuid != null) {
            webhookMessage.setAvatarUrl("https://crafatar.com/avatars/$uuid")
        }

        donorWebhook?.send(webhookMessage.build())
    }

    fun sendEmbed(message: String, uuid: UUID?, color: Int) {
        if (webhook == null) {
            ChatOG.plugin.logger.warning("Webhook has not been set or is invalid")
            return
        }

        var iconUrl: String? = null
        if (uuid != null) {
            iconUrl = "https://crafatar.com/avatars/$uuid"
        }

        val webhookMessage = WebhookEmbedBuilder()
            .setColor(color)
            .setAuthor(WebhookEmbed.EmbedAuthor(message, iconUrl, null))

        webhook?.send(webhookMessage.build())
    }
}
