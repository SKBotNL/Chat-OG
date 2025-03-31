package nl.skbotnl.chatog

import io.github.crackthecodeabhi.kreds.connection.Endpoint
import io.github.crackthecodeabhi.kreds.connection.KredsClient
import io.github.crackthecodeabhi.kreds.connection.newClient
import java.util.*
import java.util.logging.Level

object LanguageDatabase {
    private lateinit var client: KredsClient

    fun init() {
        client = newClient(Endpoint.from(Config.redisUrl))
    }

    suspend fun getPlayerLanguage(uuid: UUID): String? {
        try {
            return client.get("chatog:language:$uuid")
        } catch (e: Exception) {
            ChatOG.plugin.logger.log(Level.SEVERE, "Exception:", e)
        }
        return null
    }

    /**
     * @return True if failed
     */
    suspend fun setPlayerLanguage(uuid: UUID, language: String): Boolean {
        try {
            client.set("chatog:language:$uuid", language)
            return false
        }
        catch (e: Exception) {
            ChatOG.plugin.logger.log(Level.SEVERE, "Exception:", e)
        }
        return true
    }
}
