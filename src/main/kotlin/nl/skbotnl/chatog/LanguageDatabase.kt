package nl.skbotnl.chatog

import io.github.crackthecodeabhi.kreds.connection.Endpoint
import io.github.crackthecodeabhi.kreds.connection.KredsClient
import io.github.crackthecodeabhi.kreds.connection.newClient
import java.util.*

object LanguageDatabase {
    private lateinit var client: KredsClient

    fun init() {
        client = newClient(Endpoint.from(Config.redisUrl))
    }

    suspend fun getPlayerLanguage(uuid: UUID): String {
        return client.get("chatog:language$uuid") ?: "null"
    }

    suspend fun setPlayerLanguage(uuid: UUID, language: String) {
        client.set("chatog:language$uuid", language)
    }
}