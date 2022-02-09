package io.github.rothes.bungeepluginmanagerplus.bungeecord.api

import io.github.rothes.bungeepluginmanagerplus.api.ProxyEvent
import io.github.rothes.bungeepluginmanagerplus.api.ProxyEventHandler
import io.github.rothes.bungeepluginmanagerplus.api.ProxyPlugin
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventHandlerMethod
import java.lang.reflect.Method

class ProxyEventHandlerImpl private constructor(
    override val priority: Byte,
    override val event: ProxyEvent,
    override val plugin: ProxyPlugin,
    override val method: Method,
    override val handle: Any,
) : ProxyEventHandler {

    override fun equals(other: Any?): Boolean {
        return other is ProxyEventHandlerImpl && other.handle === this.handle
    }

    override fun hashCode(): Int {
        return handle.hashCode()
    }

    override fun toString(): String {
        return "ProxyEventHandlerImpl{priority=$priority, event=$event, plugin=$plugin, method=$method, handle=$handle}"
    }

    companion object Factory {

        fun create(event: ProxyEvent, plugin: ProxyPlugin, handler: EventHandlerMethod): ProxyEventHandler {
            val priority = handler.method.getAnnotation(EventHandler::class.java).priority
            return ProxyEventHandlerImpl(priority, event, plugin, handler.method, handler)
        }

    }

}