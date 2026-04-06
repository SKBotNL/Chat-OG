package nl.skbotnl.chatog.util

import java.util.*
import nl.skbotnl.chatog.chatsystem.ChatSystem
import nl.skbotnl.chatog.chatsystem.GeneralChatSystem
import org.bukkit.entity.Player

internal object PlayerExtensions {
    private val chatSystemMap = WeakHashMap<Player, ChatSystem>()
    var Player.chatSystem: ChatSystem
        get() = chatSystemMap[this] ?: GeneralChatSystem
        set(value) {
            chatSystemMap[this] = value
        }
}
