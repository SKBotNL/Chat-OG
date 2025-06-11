package nl.skbotnl.chatog

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URI
import java.util.concurrent.TimeUnit

object BlocklistManager {
    private var blockList = mutableListOf<String>()

    fun load() {
        ChatOG.plugin.logger.info("Loading the blocklists...")

        refresh()

        ChatOG.plugin.logger.info("Loaded the blocklists!")

        ChatOG.scope.launch {
            while (true) {
                delay(TimeUnit.DAYS.toMillis(1))
                refresh()
            }
        }
    }

    private fun refresh() {
        URI("https://raw.githubusercontent.com/hagezi/dns-blocklists/main/domains/multi.txt").toURL().openStream()
            .use { input ->
                input.bufferedReader().use { bufferedReader ->
                    bufferedReader.lines().skip(11).forEach {
                        blockList += it
                    }
                }
            }
    }

    private val urlRegex = Regex("^(https?|ftp|file)://([-a-zA-Z0-9+&@#/%?=~_|!:,.;]*?[^/]*)")
    fun checkUrl(url: String): Boolean {
        var baseUrl: String? = null
        urlRegex.findAll(url).iterator().forEach { urlMatch ->
            baseUrl = urlMatch.groups[2]?.value
        }
        if (baseUrl == null) {
            return false
        }

        blockList.forEach {
            if (it == baseUrl) {
                return true
            }
        }
        return false
    }
}