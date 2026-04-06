package nl.skbotnl.chatog.config

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import nl.skbotnl.chatog.config.discord.DiscordConfig

data class ConfigModel
@JsonCreator
constructor(
    @param:JsonProperty("prefix") val prefix: String,
    @param:JsonProperty("blocklist") val blocklist: BlocklistConfig,
    @param:JsonProperty("redis") val redis: RedisConfig,
    @param:JsonProperty("openai") val openai: OpenAIConfig,
    @param:JsonProperty("discord") val discord: DiscordConfig,
)
