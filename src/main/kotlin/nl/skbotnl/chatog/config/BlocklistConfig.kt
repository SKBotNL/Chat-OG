package nl.skbotnl.chatog.config

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class BlocklistConfig
@JsonCreator
constructor(
    @param:JsonProperty("enabled") val enabled: Boolean,
    @param:JsonProperty("blocklists") val blocklists: List<String>,
)
