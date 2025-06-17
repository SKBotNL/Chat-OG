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
import java.util.*
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
import net.trueog.utilitiesog.UtilitiesOG
import nl.skbotnl.chatog.Helper.convertMentions
import nl.skbotnl.chatog.Helper.legacyToMm
import nl.skbotnl.chatog.Helper.removeColor
import nl.skbotnl.chatog.Helper.stripGroupMentions
import nl.skbotnl.chatog.commands.TranslateMessage
import org.bukkit.Bukkit

object DiscordBridge {
    var jda: JDA? = null
    var webhook: WebhookClient? = null
    var staffWebhook: WebhookClient? = null
    var premiumWebhook: WebhookClient? = null

    private val urlRegex =
        Regex("(.*)((https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;()]*[-a-zA-Z0-9+&@#/%=~_|()])(.*)")

    fun main() {
        try {
            webhook = WebhookClient.withUrl(Config.webhook)
        } catch (e: IllegalArgumentException) {
            ChatOG.plugin.logger.warning("Config option \"webhook\" is invalid")
        }
        try {
            staffWebhook = WebhookClient.withUrl(Config.staffWebhook)
        } catch (e: IllegalArgumentException) {
            ChatOG.plugin.logger.warning("Config option \"staffWebhook\" is invalid")
        }
        try {
            premiumWebhook = WebhookClient.withUrl(Config.premiumWebhook)
        } catch (e: IllegalArgumentException) {
            ChatOG.plugin.logger.warning("Config option \"premiumWebhook\" is invalid")
        }

        jda =
            light(Config.botToken, enableCoroutines = true) {
                intents += listOf(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT)
                cache += listOf(CacheFlag.EMOJI)
                setMemberCachePolicy(MemberCachePolicy.ALL)
            }

        jda?.presence?.setPresence(Activity.playing(Config.status), false)

        jda?.listener<ReadyEvent> {
            sendMessageWithBot(Config.serverHasStartedMessage)
            jda?.getGuildById(Config.guildId)
                ?.upsertCommand(Config.listCommandName, "List all online players.")
                ?.queue()
        }

        jda?.listener<SlashCommandInteractionEvent> {
            if (it.name == Config.listCommandName) {
                it.deferReply().queue()
                try {
                    if (Bukkit.getOnlinePlayers().isEmpty()) {
                        it.hook.sendMessage("There are no players online.").queue()
                        return@listener
                    }
                    it.hook
                        .sendMessage(
                            "${
                            Config.listCommandText
                                .replace("%onlineplayers%", Bukkit.getOnlinePlayers().count().toString())
                                .replace("%maxplayers%", Bukkit.getMaxPlayers().toString())
                        }\n${
                            Bukkit.getOnlinePlayers().joinToString(separator = ", ") { player -> player.name }
                        }"
                        )
                        .queue()
                } catch (e: Exception) {
                    ChatOG.plugin.logger.warning("Could not respond to /list interaction")
                }
            }
        }

        jda?.listener<MessageReceivedEvent> {
            if (
                it.channel.id != Config.channelId &&
                    (if (Config.staffDiscordEnabled) it.channel.id != Config.staffChannelId else true) &&
                    (if (Config.premiumDiscordEnabled) it.channel.id != Config.premiumChannelId else true)
            ) {
                return@listener
            }

            if (it.author.isBot) {
                return@listener
            }

            val message = it.message

            val user = it.author
            val member = it.member ?: return@listener
            val color = member.color
            val textColor: TextColor =
                if (color == null) {
                    TextColor.color(153, 170, 181)
                } else {
                    TextColor.color(color.rgb)
                }

            val roles = member.roles
            val roleIds = roles.map { role -> role.id }

            val topRole = roles.maxBy { role -> role.positionRaw }

            val discordComponent = Component.text("Discord: ").color(TextColor.color(88, 101, 242))
            val userComponent: TextComponent =
                if (color == null) {
                    Component.text("@${user.name}").color(NamedTextColor.GRAY)
                } else {
                    Component.text("[#${topRole.name}] @${user.name}").color(textColor)
                }

            val messageColor =
                if (Config.roles.isEmpty()) {
                    NamedTextColor.GRAY
                } else {
                    val roleId =
                        message.guild
                            .getRoleById(
                                Config.roles.filter { role -> topRole.id == message.guild.getRoleById(role)?.id }[0]
                            )
                            ?.id
                    if (roleId != null) {
                        val roleColor = Config.roleMessageColor[roleId]

                        if (roleColor is Config.RGBColor) {
                            TextColor.color(roleColor.r, roleColor.g, roleColor.b)
                        } else {
                            roleColor as NamedTextColor
                        }
                    } else NamedTextColor.GRAY
                }

            val messageComponents = mutableListOf<Component>(Component.text(" >").color(messageColor))

            val attachmentComponents = mutableListOf<Component>()

            if (message.stickers.size != 0)
                attachmentComponents += Component.text("[Sticker: ${message.stickers[0].name}]").color(messageColor)

            if (message.attachments.isNotEmpty()) {
                message.attachments.forEach { attachment ->
                    var linkComponent = Component.text(attachment.url).color(TextColor.color(34, 100, 255))

                    linkComponent =
                        linkComponent.hoverEvent(
                            HoverEvent.hoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                UtilitiesOG.trueogColorize("<green>Click to open link"),
                            )
                        )

                    linkComponent =
                        linkComponent.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, attachment.url))

                    attachmentComponents += linkComponent
                }
            }

            if (message.contentDisplay != "") {
                for (word in EmojiConverter.replaceEmojisWithNames(message.contentDisplay).split(" ")) {
                    val urlIter = urlRegex.findAll(word).iterator()
                    if (urlIter.hasNext()) {
                        for (url in urlIter) {
                            if (BlocklistManager.checkUrl(word)) {
                                for (player in Bukkit.getOnlinePlayers()) {
                                    if (player.hasPermission("group.moderator")) {
                                        player.sendMessage(
                                            UtilitiesOG.trueogColorize(
                                                "${Config.prefix}<reset>: @${user.name} has posted a disallowed link: ${
                                                    word.replace(
                                                        ".",
                                                        "[dot]",
                                                    )
                                                }."
                                            )
                                        )
                                    }
                                }
                                return@listener
                            }

                            var linkComponent =
                                Component.text(url.groups[2]!!.value).color(TextColor.color(34, 100, 255))
                            linkComponent =
                                linkComponent.hoverEvent(
                                    HoverEvent.hoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        UtilitiesOG.trueogColorize("<green>Click to open link"),
                                    )
                                )

                            linkComponent =
                                linkComponent.clickEvent(
                                    ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, url.groups[2]!!.value)
                                )

                            val beforeComponent = Component.text(url.groups[1]?.value ?: "").color(messageColor)
                            val afterComponent = Component.text(url.groups[4]?.value ?: "").color(messageColor)

                            val fullComponent =
                                Component.join(
                                    JoinConfiguration.noSeparators(),
                                    beforeComponent,
                                    linkComponent,
                                    afterComponent,
                                )
                            messageComponents += fullComponent
                        }
                        continue
                    }

                    var messageText: Component = Component.text(word)
                    if (Config.colorCodeRoles.any { colorCodeRole -> colorCodeRole in roleIds }) {
                        messageText =
                            if (messageComponents.isNotEmpty()) {
                                val lastContent = (messageComponents.last() as TextComponent).content()
                                if (
                                    Helper.getColorSection(lastContent) != "" && Helper.getFirstColorSection(word) == ""
                                ) {
                                    UtilitiesOG.trueogColorize(legacyToMm(Helper.getColorSection(lastContent) + word))
                                } else {
                                    UtilitiesOG.trueogColorize(legacyToMm(word))
                                }
                            } else {
                                UtilitiesOG.trueogColorize(legacyToMm(word))
                            }
                    }
                    messageComponents += messageText.color(messageColor)
                }
            }

            val contentComponent =
                Component.join(
                    JoinConfiguration.separator(Component.text(" ")),
                    messageComponents + attachmentComponents,
                )

            var replyComponent = Component.text("")

            if (message.messageReference != null) {
                val replyMessage = message.messageReference!!.message
                if (replyMessage != null) {
                    replyComponent =
                        Component.text(
                                "[Reply to: ${if (!replyMessage.isWebhookMessage) "@" else ""}${replyMessage.author.name}] "
                            )
                            .color(NamedTextColor.GREEN)
                }
            }

            var messageComponent =
                when (it.channel.id) {
                    Config.staffChannelId -> {
                        val staffComponent = Component.text("STAFF | ").color(NamedTextColor.RED)
                        Component.join(
                            JoinConfiguration.noSeparators(),
                            discordComponent,
                            staffComponent,
                            replyComponent,
                            userComponent,
                            contentComponent,
                        )
                    }

                    Config.premiumChannelId -> {
                        val premiumComponent = Component.text("PREMIUM | ").color(NamedTextColor.GREEN)
                        Component.join(
                            JoinConfiguration.noSeparators(),
                            discordComponent,
                            premiumComponent,
                            replyComponent,
                            userComponent,
                            contentComponent,
                        )
                    }

                    else -> {
                        Component.join(
                            JoinConfiguration.noSeparators(),
                            discordComponent,
                            replyComponent,
                            userComponent,
                            contentComponent,
                        )
                    }
                }

            messageComponent =
                messageComponent.hoverEvent(
                    HoverEvent.hoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        UtilitiesOG.trueogColorize("<green>Click to translate this message"),
                    )
                )

            val randomUUID = UUID.randomUUID()
            messageComponent =
                messageComponent.clickEvent(
                    ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/translatemessage $randomUUID 2")
                )

            when (it.channel.id) {
                Config.staffChannelId -> {
                    for (p in Bukkit.getOnlinePlayers()) {
                        if (p.hasPermission("chat-og.staff")) {
                            p.sendMessage(messageComponent)
                        }
                    }
                }

                Config.premiumChannelId -> {
                    for (p in Bukkit.getOnlinePlayers()) {
                        if (p.hasPermission("chat-og.premium")) {
                            p.sendMessage(messageComponent)
                        }
                    }
                }

                else -> {
                    for (p in Bukkit.getOnlinePlayers()) {
                        p.sendMessage(messageComponent)
                    }
                }
            }

            TranslateMessage.customMessages[randomUUID] =
                TranslateMessage.SentCustomMessage(
                    message.contentDisplay,
                    member.effectiveName,
                    Component.join(JoinConfiguration.noSeparators(), discordComponent, userComponent),
                    Component.text(" > ").color(messageColor),
                )
        }
    }

    fun sendMessageWithBot(message: String) {
        if (!Config.discordEnabled) return

        val channel = jda?.getChannel<MessageChannel>(Config.channelId)
        if (channel == null) {
            ChatOG.plugin.logger.warning("channelId has not been set or is invalid")
        }

        channel?.sendMessage(message)?.complete()
    }

    suspend fun sendMessage(message: String, player: String, uuid: UUID?) {
        if (!Config.discordEnabled) return

        if (webhook == null) {
            ChatOG.plugin.logger.warning("webhook has not been set or is invalid")
            return
        }

        val webhookMessage =
            WebhookMessageBuilder()
                .setUsername(removeColor(player))
                .setContent(stripGroupMentions(convertMentions(message)))
        if (uuid != null) {
            webhookMessage.setAvatarUrl("https://minotar.net/helm/$uuid.png")
        }

        webhook?.send(webhookMessage.build())
    }

    suspend fun sendStaffMessage(message: String, player: String, uuid: UUID?) {
        if (!Config.discordEnabled || !Config.staffDiscordEnabled) return

        if (staffWebhook == null) {
            ChatOG.plugin.logger.warning("staffWebhook has not been set or is invalid")
            return
        }

        val webhookMessage =
            WebhookMessageBuilder()
                .setUsername(removeColor(player))
                .setContent(stripGroupMentions((convertMentions(message))))
        if (uuid != null) {
            webhookMessage.setAvatarUrl("https://minotar.net/helm/$uuid.png")
        }

        staffWebhook?.send(webhookMessage.build())
    }

    suspend fun sendPremiumMessage(message: String, player: String, uuid: UUID?) {
        if (!Config.discordEnabled || !Config.premiumDiscordEnabled) return

        if (premiumWebhook == null) {
            ChatOG.plugin.logger.warning("premiumWebhook has not been set or is invalid")
            return
        }

        val webhookMessage =
            WebhookMessageBuilder()
                .setUsername(removeColor(player))
                .setContent(stripGroupMentions((convertMentions(message))))
        if (uuid != null) {
            webhookMessage.setAvatarUrl("https://minotar.net/helm/$uuid.png")
        }

        premiumWebhook?.send(webhookMessage.build())
    }

    fun sendEmbed(message: String, uuid: UUID?, color: Int) {
        if (!Config.discordEnabled) return

        if (webhook == null) {
            ChatOG.plugin.logger.warning("Webhook has not been set or is invalid")
            return
        }

        var iconUrl: String? = null
        if (uuid != null) {
            iconUrl = "https://minotar.net/helm/$uuid.png"
        }

        val webhookMessage =
            WebhookEmbedBuilder().setColor(color).setAuthor(WebhookEmbed.EmbedAuthor(message, iconUrl, null))

        webhook?.send(webhookMessage.build())
    }
}
