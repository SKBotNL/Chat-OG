package nl.skbotnl.chatog.config

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class RedisConfig
@JsonCreator
constructor(
    @param:JsonProperty("host") val host: String,
    @param:JsonProperty("port") val port: Int,
    @param:JsonProperty("database") val database: Int,
    @param:JsonProperty("password") val password: String?,
)
