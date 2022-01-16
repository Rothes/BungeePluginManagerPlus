package io.github.rothes.bungeepluginmanagerplus.bungeecord.events

import io.github.rothes.bungeepluginmanagerplus.api.Action
import io.github.rothes.bungeepluginmanagerplus.api.ProxyPlugin
import java.io.File

class PluginUnloadEvent(action: Action, plugin: ProxyPlugin, pluginFile: File, id: Short) :
    AbstractBpmpEvent(action, plugin, pluginFile, id)