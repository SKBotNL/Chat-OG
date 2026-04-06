package nl.skbotnl.chatog.util

import java.net.URI
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nl.skbotnl.chatog.ChatOG.Companion.config
import nl.skbotnl.chatog.ChatOG.Companion.plugin
import nl.skbotnl.chatog.ChatOG.Companion.scope

internal class BlocklistManager {
    private val lock = Any()
    private val blockList = mutableListOf<String>()

    init {
        plugin.logger.info("Loading the blocklists...")
        refresh()
        plugin.logger.info("Loaded the blocklists!")

        scope.launch {
            while (true) {
                delay(TimeUnit.DAYS.toMillis(1))
                refresh()
            }
        }
    }

    private fun refresh() {
        synchronized(lock) {
            blockList.clear()
            config.blocklist.blocklists.forEach { blocklist ->
                URI(blocklist).toURL().openStream().use { input ->
                    input.bufferedReader().use { bufferedReader ->
                        bufferedReader.lines().forEach { if (!it.startsWith("#")) blockList.add(it) }
                    }
                }
            }
        }
    }

    private val urlRegex = Regex("(?:https?://)?([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})")

    fun checkUrl(url: String): Boolean {
        val match = urlRegex.find(url) ?: return false
        val baseUrl = match.groups[1]?.value ?: return false
        return synchronized(lock) { blockList.contains(baseUrl) }
    }
}
