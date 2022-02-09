package io.github.rothes.bungeepluginmanagerplus.api

interface ProxyPlugin {

    val name: String
    val handle: Any

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int

}