package io.github.rothes.bungeepluginmanagerplus.bungeecord.events

import io.github.rothes.bungeepluginmanagerplus.api.Action
import net.md_5.bungee.api.plugin.Cancellable
import net.md_5.bungee.api.plugin.Event
import java.io.File

class PluginLoadEvent(
    val action: Action,
    val pluginName: String,
    val pluginFile: File,
    val id : Short,
) : Event(), Cancellable {

    var cancelledMessage: String? = null
    private var cancelled = false

    override fun isCancelled(): Boolean {
        return cancelled
    }

    override fun setCancelled(cancel: Boolean) {
        cancelled = cancel
    }

}