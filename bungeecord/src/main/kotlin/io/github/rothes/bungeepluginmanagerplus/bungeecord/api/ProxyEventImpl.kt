package io.github.rothes.bungeepluginmanagerplus.bungeecord.api

import io.github.rothes.bungeepluginmanagerplus.api.ProxyEvent
import net.md_5.bungee.api.plugin.Event

class ProxyEventImpl private constructor(
    override val name: String,
    override val clazz: Class<out Any>,
) : ProxyEvent {

    override fun toString(): String {
        return "ProxyEventImpl{name=$name, clazz=$clazz}"
    }

    companion object Factory {

        fun create(clazz: Class<out Event>): ProxyEvent {
            return ProxyEventImpl(clazz.simpleName, clazz)
        }

    }

}
