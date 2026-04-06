package nl.skbotnl.chatog.commands

import nl.skbotnl.chatog.chatsystem.PremiumChatSystem

internal class PremiumChat : ChatSystemChat<PremiumChatSystem>() {
    override val chatSystem = PremiumChatSystem
    override val permission = "chat-og.premium"
}
