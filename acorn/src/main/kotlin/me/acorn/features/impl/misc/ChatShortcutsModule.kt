package me.acorn.features.impl.misc

import me.acorn.Acorn
import me.melinoe.clickgui.settings.impl.SelectorSetting
import me.melinoe.features.Module

object ChatShortcutsModule : Module(
    name = "Chat Shortcuts",
    description = "Configure /ac, /gc, and /cc commands.",
    category = Acorn.CATEGORY
) {
    private val defaultChatSetting = SelectorSetting(
        "Default Chat", "Default",
        listOf("Default", "Guild", "Group"),
        desc = "Chat mode to return to after sending a message."
    )
    val defaultChat by defaultChatSetting

    val restoreCommand: String
        get() = when (defaultChatSetting.selected) {
            "Guild" -> "chat guild"
            "Group" -> "chat group"
            else -> "chat default"
        }
}
