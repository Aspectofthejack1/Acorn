package me.acorn

import me.acorn.commands.acCommand
import me.acorn.commands.ccCommand
import me.acorn.commands.gcCommand
import me.acorn.commands.AcornChatSuppressor
import me.acorn.features.impl.misc.AutoReactModule
import me.acorn.features.impl.misc.ChatShortcutsModule
import me.melinoe.config.ModuleConfig
import me.melinoe.events.core.EventBus
import me.melinoe.features.Category
import me.melinoe.features.ModuleManager
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback

object Acorn : ClientModInitializer {

    val CATEGORY: Category = Category.custom("Acorn")

    override fun onInitializeClient() {

        ModuleManager.registerModules(
            ModuleConfig("acorn-config.json"),
            AutoReactModule,
            ChatShortcutsModule
        )

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            arrayOf(acCommand, gcCommand, ccCommand).forEach { it.register(dispatcher) }
        }

        EventBus.subscribe(AcornChatSuppressor)
    }
}
