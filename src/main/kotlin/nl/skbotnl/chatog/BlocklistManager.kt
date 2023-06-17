package nl.skbotnl.chatog

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URL
import java.util.concurrent.TimeUnit

object BlocklistManager {
    private var blockList = mutableListOf<String>()

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun load() {
        ChatOG.plugin.logger.info("Loading the blocklists...")

        refresh()

        ChatOG.plugin.logger.info("Loaded the blocklists!")

        GlobalScope.launch {
            while (true) {
                delay(TimeUnit.DAYS.toMillis(1))
                refresh()
            }
        }
    }

    private fun refresh() {
        val tempList = mutableListOf<String>()

        URL("https://blocklistproject.github.io/Lists/alt-version/abuse-nl.txt").openStream().use { input ->
            input.bufferedReader().use { bufferedReader ->
                bufferedReader.lines().skip(15).forEach {
                    tempList += it
                }
            }
        }
        URL("https://blocklistproject.github.io/Lists/alt-version/crypto-nl.txt").openStream().use { input ->
            input.bufferedReader().use { bufferedReader ->
                bufferedReader.lines().skip(15).forEach {
                    tempList += it
                }
            }
        }
        URL("https://blocklistproject.github.io/Lists/alt-version/fraud-nl.txt").openStream().use { input ->
            input.bufferedReader().use { bufferedReader ->
                bufferedReader.lines().skip(15).forEach {
                    tempList += it
                }
            }
        }
        URL("https://blocklistproject.github.io/Lists/alt-version/malware-nl.txt").openStream().use { input ->
            input.bufferedReader().use { bufferedReader ->
                bufferedReader.lines().skip(15).forEach {
                    tempList += it
                }
            }
        }
        URL("https://blocklistproject.github.io/Lists/alt-version/phishing-nl.txt").openStream().use { input ->
            input.bufferedReader().use { bufferedReader ->
                bufferedReader.lines().skip(15).forEach {
                    tempList += it
                }
            }
        }
        URL("https://blocklistproject.github.io/Lists/alt-version/porn-nl.txt").openStream().use { input ->
            input.bufferedReader().use { bufferedReader ->
                bufferedReader.lines().forEach {
                    tempList += it
                }
            }
        }
        URL("https://blocklistproject.github.io/Lists/alt-version/ransomware-nl.txt").openStream().use { input ->
            input.bufferedReader().use { bufferedReader ->
                bufferedReader.lines().forEach {
                    tempList += it
                }
            }
        }
        URL("https://blocklistproject.github.io/Lists/alt-version/scam-nl.txt").openStream().use { input ->
            input.bufferedReader().use { bufferedReader ->
                bufferedReader.lines().forEach {
                    tempList += it
                }
            }
        }

        blockList = tempList
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
            if (it == baseUrl!!) {
                return true
            }
        }
        return false
    }
}