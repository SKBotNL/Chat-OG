package nl.skbotnl.chatog.config.discord

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class DiscordSuffixConfig @JsonCreator constructor(@param:JsonProperty("suffix") val suffix: String)
