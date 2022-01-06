package io.github.rothes.bungeepluginmanagerplus.bungeecord.api

import io.github.rothes.bungeepluginmanagerplus.api.ProxyCommand
import io.github.rothes.bungeepluginmanagerplus.api.ProxyPlugin
import net.md_5.bungee.api.plugin.Command

class ProxyCommandImpl private constructor(
    override val name: String,
    override val permission: String?,
    override val aliases: Array<String>,
    override val permissionMessage: String?,
    override val handle: Any,
    override val plugin: ProxyPlugin
) : ProxyCommand {

    companion object Factory {

        @JvmStatic
        fun create (
            handle: Command,
            plugin: ProxyPlugin
        ): ProxyCommandImpl {
            return ProxyCommandImpl(handle.name, handle.permission, handle.aliases, handle.permissionMessage, handle, plugin)
        }

    }

}