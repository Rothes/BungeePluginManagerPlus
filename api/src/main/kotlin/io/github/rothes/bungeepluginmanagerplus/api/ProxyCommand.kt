package io.github.rothes.bungeepluginmanagerplus.api

interface ProxyCommand {

    val name: String
    val permission: String?
    val aliases: Array<String>
    val permissionMessage: String?
    val handle: Any
    val plugin: ProxyPlugin

}