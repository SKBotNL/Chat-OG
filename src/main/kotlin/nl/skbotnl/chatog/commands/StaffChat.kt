package nl.skbotnl.chatog.commands

import nl.skbotnl.chatog.chatsystem.StaffChatSystem

internal class StaffChat : ChatSystemChat<StaffChatSystem>() {
    override val chatSystem = StaffChatSystem
    override val permission = "chat-og.staff"
}
