package io.github.rothes.bungeepluginmanagerplus.api

interface ProxyCommand {

    val name: String
    val permission: String?
    val aliases: Array<String>
    val permissionMessage: String?
    val plugin: ProxyPlugin
    val handle: Any

}