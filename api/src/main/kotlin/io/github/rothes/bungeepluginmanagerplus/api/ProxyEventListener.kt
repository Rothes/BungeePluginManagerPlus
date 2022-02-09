package io.github.rothes.bungeepluginmanagerplus.api

interface ProxyEventListener {

    val clazz: Class<Any>
    val plugin: ProxyPlugin
    val handle: Any

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

}