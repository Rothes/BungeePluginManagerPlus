package io.github.rothes.bungeepluginmanagerplus.bungeecord.events

import io.github.rothes.bungeepluginmanagerplus.api.Action
import io.github.rothes.bungeepluginmanagerplus.api.ProxyEventHandler
import io.github.rothes.bungeepluginmanagerplus.api.ProxyPlugin
import java.io.File

class PluginEventHandlerRemoveEvent(action: Action, plugin: ProxyPlugin, pluginFile: File, handler: ProxyEventHandler, id: Short) :
    AbstractBpmpEvent(action, plugin, pluginFile, id)