package nl.skbotnl.chatog.config.discord

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import net.kyori.adventure.text.format.TextColor
import tools.jackson.databind.annotation.JsonDeserialize

data class DiscordRoleConfig
@JsonCreator
constructor(
    @field:JsonDeserialize(using = TextColorDeserializer::class)
    @param:JsonProperty("messageColor")
    val messageColor: TextColor
)
