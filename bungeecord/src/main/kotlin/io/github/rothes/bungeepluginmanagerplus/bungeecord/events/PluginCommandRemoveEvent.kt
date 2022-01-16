package io.github.rothes.bungeepluginmanagerplus.bungeecord.events

import io.github.rothes.bungeepluginmanagerplus.api.Action
import io.github.rothes.bungeepluginmanagerplus.api.ProxyCommand
import io.github.rothes.bungeepluginmanagerplus.api.ProxyPlugin
import java.io.File

class PluginCommandRemoveEvent(action: Action, plugin: ProxyPlugin, pluginFile: File, command: ProxyCommand, id: Short) :
    AbstractBpmpEvent(action, plugin, pluginFile, id)