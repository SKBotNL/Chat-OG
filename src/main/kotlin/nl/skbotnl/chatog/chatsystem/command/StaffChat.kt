package nl.skbotnl.chatog.chatsystem.command

import nl.skbotnl.chatog.chatsystem.StaffChatSystem

internal class StaffChat : ChatSystemChat<StaffChatSystem>() {
    override val chatSystem = StaffChatSystem
    override val permission = "chat-og.staff"
}
