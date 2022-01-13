package io.github.rothes.bungeepluginmanagerplus.bungeecord.internal

import io.github.rothes.bungeepluginmanagerplus.api.Action
import io.github.rothes.bungeepluginmanagerplus.bungeecord.events.PluginUnloadEvent
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.event.ServerSwitchEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority
import java.util.concurrent.TimeUnit

object Listeners : Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onBpmpUnloadEvent(e: PluginUnloadEvent) {
        if (e.action == Action.PLUGIN_UNLOAD && e.plugin.name == "BungeePluginManagerPlus") {
            e.isCancelled = true
            e.cancelledMessage = I18nHelper.getLocaleMessage("Sender.Event.Cancelled-Reasons.Cannot-Unload-Bpmp")
        }
    }

    @EventHandler
    fun onJoinServer(e: ServerSwitchEvent) {
        if (Updater.newVersionMsg != null && e.player.hasPermission("bungeepluginmanagerplus.admin")) {
            ProxyServer.getInstance().scheduler.schedule(plugin, {
                for (msg in Updater.newVersionMsg!!) {
                    e.player.sendMessage(msg)
                }
            }, 1, TimeUnit.SECONDS)
        }
    }

}