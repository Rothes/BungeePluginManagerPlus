package io.github.rothes.bungeepluginmanagerplus.bungeecord.events

import io.github.rothes.bungeepluginmanagerplus.api.Action
import io.github.rothes.bungeepluginmanagerplus.api.ProxyEventListener
import io.github.rothes.bungeepluginmanagerplus.api.ProxyPlugin
import java.io.File

class PluginEventListenerRemoveEvent(action: Action, plugin: ProxyPlugin, pluginFile: File, listener: ProxyEventListener, id: Short) :
    AbstractBpmpEvent(action, plugin, pluginFile, id)