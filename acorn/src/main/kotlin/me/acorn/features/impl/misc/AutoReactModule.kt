package me.acorn.features.impl.misc

import me.acorn.Acorn
import me.melinoe.Melinoe.mc
import me.melinoe.clickgui.settings.Setting.Companion.withDependency
import me.melinoe.clickgui.settings.impl.BooleanSetting
import me.melinoe.clickgui.settings.impl.StringSetting
import me.melinoe.events.ChatPacketEvent
import me.melinoe.events.core.on
import me.melinoe.events.core.onReceive
import me.melinoe.features.Module
import me.melinoe.utils.TabListUtils
import me.melinoe.utils.data.Item
import me.melinoe.utils.handlers.schedule
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket

object AutoReactModule : Module(
    name = "Auto React",
    description = "Automatically says /ac f on player deaths and /ac gg on notable drops.",
    category = Acorn.CATEGORY
) {
    val autoF by BooleanSetting("Auto F", true, desc = "Say /ac f when a player dies.")
    val fMessage by StringSetting("F Message", "f", desc = "Message to send on a death.").withDependency { autoF }

    val autoGG by BooleanSetting("Auto GG", true, desc = "Say /ac gg on notable drops.")
    val ggMessage by StringSetting("GG Message", "gg", desc = "Message to send on a notable drop.").withDependency { autoGG }
    val ggBloodshot by BooleanSetting("Bloodshot", true, desc = "React to Bloodshot drops.").withDependency { autoGG }
    val ggUnholy by BooleanSetting("Unholy", true, desc = "React to Unholy drops.").withDependency { autoGG }
    val ggCompanion by BooleanSetting("Companion", true, desc = "React to Companion drops.").withDependency { autoGG }
    val ggTranscend by BooleanSetting("Transcend", true, desc = "React when a player fully transcends a class.").withDependency { autoGG }

    private var lastSent = 0L
    private const val COOLDOWN_MS = 1500L

    // Matches all 9 death formats, requires [X Lvl Y Class]<unicode> to prevent player fakes
    private val DEATH_PATTERN = Regex(
        """^\[.+\] \[.+ Lvl \d+ \w+\].+(?:fell off against|dieded by|got snapped in half by|was torn in half by|had their day ruined by|had their head removed by|was diagnosed with 'skill issue' by|had their bits blown off by|was spangled by) .+ and gained [\d,]+ glory\.$"""
    )

    // Extracts city name from a "[City, Hub-X]" prefix (e.g. "New York" from "[New York, Hub-1]...")
    private val MSG_LOCATION_PATTERN = Regex("""^\[(.+?),\s*Hub-\d+\]""")

    /**
     * Returns true if the message's [City, Hub-X] prefix matches the player's current server city.
     * Hub number is ignored — only the city is compared so cross-hub same-city broadcasts still fire.
     * Falls back to true if either side can't be determined, so we don't silently swallow reactions.
     */
    private fun isOnSameServer(msg: String): Boolean {
        val msgCity = MSG_LOCATION_PATTERN.find(msg)?.groupValues?.get(1) ?: return true
        val tabServer = TabListUtils.getServer() ?: return true // e.g. "[New York, Hub-1]"
        val tabCity = MSG_LOCATION_PATTERN.find(tabServer)?.groupValues?.get(1) ?: return true
        return msgCity == tabCity
    }

    // Matches server transcendence broadcasts — only fires on system packets so players can't fake it.
    // e.g. "[New York, Hub-1]𕼘𘁖 bigfatjuicypoo9 Has just fully transcended Samurai! (3/6)."
    private val TRANSCEND_PATTERN = Regex(
        """^\[.+\]\S* \S+ Has just fully transcended \w+! \(\d/6\)\.$"""
    )

    // Matches pity mod announcements in player chat
    // e.g. "[BLOODSHOT] Dropped Martyr at 27 pity from Raphael's Chamber!"
    private val GG_PATTERN_RARITY = Regex(
        """\[(BLOODSHOT|UNHOLY|COMPANION)\] Dropped .+ at \d+ pity from .+!"""
    )

    private fun rarityEnabled(rarity: Item.Rarity) = when (rarity) {
        Item.Rarity.BLOODSHOT -> ggBloodshot
        Item.Rarity.UNHOLY -> ggUnholy
        Item.Rarity.COMPANION -> ggCompanion
        else -> false
    }

    /**
     * Checks if [msg] is a server broadcast drop for an enabled rarity.
     * Format: "[Server] <icon> PlayerName got ItemName from Dungeon"
     */
    private fun isNotableBroadcastDrop(msg: String): Boolean {
        val gotIndex = msg.lastIndexOf(" got ")
        if (gotIndex == -1) return false
        val itemStart = gotIndex + 5
        val matched = Item.entries
            .filter { msg.indexOf(it.displayName, itemStart) == itemStart }
            .maxByOrNull { it.displayName.length }
            ?: return false
        return rarityEnabled(matched.rarity)
    }

    private fun sendGG() {
        schedule(4) {
            mc.player?.connection?.sendCommand("chat default")
            schedule(4) {
                mc.player?.connection?.sendChat(ggMessage)
            }
        }
    }

    init {
        on<ChatPacketEvent> {
            if (!enabled) return@on
            val msg = value
            val now = System.currentTimeMillis()
            if (now - lastSent < COOLDOWN_MS) return@on

            if (autoF && DEATH_PATTERN.containsMatchIn(msg) && isOnSameServer(msg)) {
                lastSent = now
                schedule(4) {
                    mc.player?.connection?.sendCommand("chat default")
                    schedule(4) {
                        mc.player?.connection?.sendChat(fMessage)
                    }
                }
                return@on
            }

            if (!autoGG) return@on

            // Check pity mod format in system messages
            val rarityMatch = GG_PATTERN_RARITY.find(msg)
            if (rarityMatch != null) {
                val enabled = when (rarityMatch.groupValues[1]) {
                    "BLOODSHOT" -> ggBloodshot
                    "UNHOLY" -> ggUnholy
                    "COMPANION" -> ggCompanion
                    else -> false
                }
                if (enabled) {
                    lastSent = now
                    sendGG()
                    return@on
                }
            }

            if (ggTranscend && TRANSCEND_PATTERN.containsMatchIn(msg) && isOnSameServer(msg)) {
                lastSent = now
                sendGG()
                return@on
            }

            if (isNotableBroadcastDrop(msg)) {
                lastSent = now
                sendGG()
            }
        }

        // ChatPacketEvent only fires for ClientboundSystemChatPacket.
        // Player chat messages (e.g. pity mod announcements in hub chat) need separate handling.
        onReceive<ClientboundPlayerChatPacket> {
            if (!enabled || !autoGG) return@onReceive
            val now = System.currentTimeMillis()
            if (now - lastSent < COOLDOWN_MS) return@onReceive
            val content = unsignedContent?.string ?: body.content
            val rarityMatch = GG_PATTERN_RARITY.find(content) ?: return@onReceive
            val rarityEnabled = when (rarityMatch.groupValues[1]) {
                "BLOODSHOT" -> ggBloodshot
                "UNHOLY" -> ggUnholy
                "COMPANION" -> ggCompanion
                else -> false
            }
            if (rarityEnabled) {
                lastSent = now
                sendGG()
            }
        }
    }
}
