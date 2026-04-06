package nl.skbotnl.chatog.chatsystem.command

import nl.skbotnl.chatog.chatsystem.DeveloperChatSystem

internal class DeveloperChat : ChatSystemChat<DeveloperChatSystem>() {
    override val chatSystem = DeveloperChatSystem
    override val permission = "chat-og.developer"
}
