package nl.skbotnl.chatog.config

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class OpenAIConfig
@JsonCreator
constructor(
    @param:JsonProperty("enabled") val enabled: Boolean,
    @param:JsonProperty("baseUrl") val baseUrl: String?,
    @param:JsonProperty("apiKey") val apiKey: String?,
    @param:JsonProperty("model") val model: String?,
)
