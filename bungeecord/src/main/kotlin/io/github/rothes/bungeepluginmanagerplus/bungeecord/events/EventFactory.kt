package io.github.rothes.bungeepluginmanagerplus.bungeecord.events

import io.github.rothes.bungeepluginmanagerplus.api.Action
import io.github.rothes.bungeepluginmanagerplus.api.ProxyCommand
import io.github.rothes.bungeepluginmanagerplus.api.ProxyEventHandler
import io.github.rothes.bungeepluginmanagerplus.api.ProxyEventListener
import io.github.rothes.bungeepluginmanagerplus.api.ProxyPlugin
import net.md_5.bungee.api.plugin.Plugin
import java.io.File

object EventFactory {

    private var currentId: Short = 0

    fun nextId(): Short {
        if (currentId == Short.MAX_VALUE)
            currentId = 0
        return currentId++
    }

    fun createPluginLoadEvent (
        action: Action,
        pluginName: String,
        pluginFile: File,
        id: Short = nextId()): PluginLoadEvent {
        return PluginLoadEvent(action, pluginName, pluginFile, id)
    }

    fun createPluginUnloadEvent (
        action: Action,
        plugin: ProxyPlugin,
        id: Short = nextId()): PluginUnloadEvent {
        return PluginUnloadEvent(action, plugin, (plugin.handle as Plugin).file, id)
    }

    fun createPluginCommandRemoveEvent (
        command: ProxyCommand,
        id: Short = nextId()): PluginCommandRemoveEvent {
        val plugin = command.plugin
        return PluginCommandRemoveEvent(Action.COMMAND_REMOVE, plugin, (plugin.handle as Plugin).file, command, id)
    }

    fun createPluginEventListenerRemoveEvent (
        listener: ProxyEventListener,
        id: Short = nextId()): PluginEventListenerRemoveEvent {
        val plugin = listener.plugin
        return PluginEventListenerRemoveEvent(Action.EVENT_LISTENER_REMOVE, plugin, (plugin.handle as Plugin).file, listener, id)
    }

    fun createPluginEventHandlerRemoveEvent (
        handler: ProxyEventHandler,
        id: Short = nextId()): PluginEventHandlerRemoveEvent {
        val plugin = handler.plugin
        return PluginEventHandlerRemoveEvent(Action.EVENT_HANDLER_REMOVE, plugin, (plugin.handle as Plugin).file, handler, id)
    }

}