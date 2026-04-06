package nl.skbotnl.chatog.commands

import nl.skbotnl.chatog.chatsystem.DeveloperChatSystem

internal class DeveloperChat : ChatSystemChat<DeveloperChatSystem>() {
    override val chatSystem = DeveloperChatSystem
    override val permission = "chat-og.developer"
}
