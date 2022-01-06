package io.github.rothes.bungeepluginmanagerplus.bungeecord.api

import io.github.rothes.bungeepluginmanagerplus.api.ProxyPlugin
import net.md_5.bungee.api.plugin.Plugin

class ProxyPluginImpl private constructor(
    override val name: String,
    override val handle: Any,
) : ProxyPlugin {

    companion object Factory {

        @JvmStatic
        fun create(plugin: Plugin): ProxyPlugin {
            return ProxyPluginImpl(plugin.description.name, plugin)
        }

    }

}