package io.github.rothes.bungeepluginmanagerplus.api

import java.util.*

interface ProxyCommand {

    val name: String
    val permission: Optional<String>
    val aliases: Array<String>
    val permissionMessage: Optional<String>
    val plugin: ProxyPlugin
    val handle: Any

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

}