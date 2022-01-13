package io.github.rothes.bungeepluginmanagerplus.bungeecord.api

import io.github.rothes.bungeepluginmanagerplus.api.ProxyCommand
import io.github.rothes.bungeepluginmanagerplus.api.ProxyPlugin
import net.md_5.bungee.api.plugin.Command

class ProxyCommandImpl private constructor(
    override val name: String,
    override val permission: String?,
    override val aliases: Array<String>,
    override val permissionMessage: String?,
    override val plugin: ProxyPlugin,
    override val handle: Any,
) : ProxyCommand {

    override fun toString(): String {
        return "ProxyCommandImpl{name=$name, permission=$permission, aliases=$aliases, permissionMessage=$permissionMessage, plugin=$plugin}, handle=$handle}"
    }

    companion object Factory {

        fun create (handle: Command, plugin: ProxyPlugin): ProxyCommandImpl {
            return ProxyCommandImpl(handle.name, handle.permission, handle.aliases, handle.permissionMessage, plugin, handle)
        }

    }

}