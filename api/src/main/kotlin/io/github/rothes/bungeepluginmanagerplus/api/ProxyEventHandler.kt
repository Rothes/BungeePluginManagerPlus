package io.github.rothes.bungeepluginmanagerplus.api

import java.lang.reflect.Method

/**
 * Represent a method in a event listener.
 */
interface ProxyEventHandler {

    val priority: ProxyEventPriority
    val method: Method
    val event: ProxyEvent
    val plugin: ProxyPlugin
    val handle: Any

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

}