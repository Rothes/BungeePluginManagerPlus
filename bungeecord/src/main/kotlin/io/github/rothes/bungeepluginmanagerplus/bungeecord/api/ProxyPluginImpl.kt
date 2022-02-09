package io.github.rothes.bungeepluginmanagerplus.bungeecord.api

import io.github.rothes.bungeepluginmanagerplus.api.ProxyPlugin
import net.md_5.bungee.api.plugin.Plugin

class ProxyPluginImpl private constructor(
    override val name: String,
    override val handle: Any,
) : ProxyPlugin {

    override fun equals(other: Any?): Boolean {
        return other is ProxyPluginImpl && other.handle === this.handle
    }

    override fun hashCode(): Int {
        return handle.hashCode()
    }

    override fun toString(): String {
        return "ProxyPluginImpl{name=$name, handle=$handle}"
    }

    companion object Factory {

        fun create(plugin: Plugin): ProxyPlugin {
            return ProxyPluginImpl(plugin.description.name, plugin)
        }

    }

}