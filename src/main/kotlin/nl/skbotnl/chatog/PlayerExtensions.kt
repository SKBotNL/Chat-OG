package nl.skbotnl.chatog

import java.util.WeakHashMap
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
