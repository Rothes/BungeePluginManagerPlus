package io.github.rothes.bungeepluginmanagerplus.api

interface ProxyEventListener {

    val clazz: Class<Any>
    val plugin: ProxyPlugin
    val handle: Any

}