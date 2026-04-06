package nl.skbotnl.chatog.config.discord

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.net.URL

class DiscordGeneralChatConfig
@JsonCreator
constructor(@param:JsonProperty("channelId") val channelId: String?, @param:JsonProperty("webhook") val webhook: URL?)
