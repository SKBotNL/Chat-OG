package nl.skbotnl.chatog.translation

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisConnectionException
import io.lettuce.core.RedisException
import io.lettuce.core.RedisURI
import java.util.UUID
import nl.skbotnl.chatog.ChatOG.Companion.config

internal class LanguageDatabase {
    private val redisClient: RedisClient =
        RedisClient.create(
            RedisURI.Builder.redis(config.redis.host, config.redis.port)
                .apply { config.redis.password?.let { withPassword(it) } }
                .withDatabase(config.redis.database)
                .build()
        )

    /** @return True if failed */
    fun testConnection(): Boolean {
        try {
            val connection = redisClient.connect()
            connection.close()
            return false
        } catch (_: RedisConnectionException) {
            return true
        }
    }

    fun getPlayerLanguage(uuid: UUID): Result<String?> {
        try {
            val connection = redisClient.connect()
            val commands = connection.sync()
            val value = commands.get("chatog:language:$uuid")
            connection.close()
            return Result.success(value)
        } catch (e: RedisException) {
            return Result.failure(e)
        }
    }

    fun setPlayerLanguage(uuid: UUID, language: String) {
        val connection = redisClient.connect()
        val commands = connection.sync()
        commands.set("chatog:language:$uuid", language)
        connection.close()
    }

    fun shutdown() {
        redisClient.shutdown()
    }
}
