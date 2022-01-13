package io.github.rothes.bungeepluginmanagerplus.bungeecord.api

import io.github.rothes.bungeepluginmanagerplus.api.ProxyEventListener
import io.github.rothes.bungeepluginmanagerplus.api.ProxyPlugin
import net.md_5.bungee.api.plugin.Listener

class ProxyEventListenerImpl private constructor(
    override val clazz: Class<Any>,
    override val plugin: ProxyPlugin,
    override val handle: Any,
) : ProxyEventListener {

    override fun toString(): String {
        return "ProxyEventListenerImpl{clazz=$clazz, plugin=$plugin, handle=$handle}"
    }

    companion object Factory {

        fun create(listener: Listener, plugin: ProxyPlugin): ProxyEventListener {
            return ProxyEventListenerImpl(listener.javaClass, plugin, listener)
        }

    }

}