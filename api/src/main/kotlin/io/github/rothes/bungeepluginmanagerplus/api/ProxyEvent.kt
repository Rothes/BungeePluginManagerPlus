package io.github.rothes.bungeepluginmanagerplus.api

interface ProxyEvent {

    val name: String
    val clazz: Class<out Any>

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int

}