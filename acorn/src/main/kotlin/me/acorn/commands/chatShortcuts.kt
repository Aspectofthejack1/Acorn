package me.acorn.commands

import com.github.stivais.commodore.Commodore
import com.github.stivais.commodore.utils.GreedyString
import me.acorn.features.impl.misc.ChatShortcutsModule
import me.melinoe.Melinoe.mc
import me.melinoe.events.ChatPacketEvent
import me.melinoe.events.core.on
import me.melinoe.utils.ChatManager.hideMessage
import me.melinoe.utils.handlers.schedule

object AcornChatSuppressor {
    init {
        on<ChatPacketEvent> {
            if (value.startsWith("Set your chat mode to")) hideMessage()
        }
    }
}

private fun restoreChat() {
    if (!ChatShortcutsModule.enabled) return
    schedule(4) {
        mc.player?.connection?.sendCommand(ChatShortcutsModule.restoreCommand)
    }
}

val acCommand = Commodore("ac") {
    runs { text: GreedyString ->
        val player = mc.player ?: return@runs
        player.connection.sendCommand("chat default")
        schedule(4) {
            mc.player?.connection?.sendChat(text.string)
            restoreChat()
        }
    }
}

val gcCommand = Commodore("gc") {
    runs { text: GreedyString ->
        val player = mc.player ?: return@runs
        player.connection.sendCommand("chat guild")
        schedule(4) {
            mc.player?.connection?.sendChat(text.string)
            restoreChat()
        }
    }
}

val ccCommand = Commodore("cc") {
    runs { text: GreedyString ->
        val player = mc.player ?: return@runs
        player.connection.sendCommand("chat group")
        schedule(4) {
            mc.player?.connection?.sendChat(text.string)
            restoreChat()
        }
    }
}
