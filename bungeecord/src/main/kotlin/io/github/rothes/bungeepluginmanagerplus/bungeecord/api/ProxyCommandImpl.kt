package io.github.rothes.bungeepluginmanagerplus.bungeecord.api

import io.github.rothes.bungeepluginmanagerplus.api.ProxyCommand
import io.github.rothes.bungeepluginmanagerplus.api.ProxyPlugin
import net.md_5.bungee.api.plugin.Command
import java.util.*

class ProxyCommandImpl private constructor(
    override val name: String,
    override val permission: Optional<String>,
    override val aliases: Array<String>,
    override val permissionMessage: Optional<String>,
    override val plugin: ProxyPlugin,
    override val handle: Any,
) : ProxyCommand {

    override fun equals(other: Any?): Boolean {
        return other is ProxyCommandImpl && other.handle === this.handle
    }

    override fun hashCode(): Int {
        return handle.hashCode()
    }

    override fun toString(): String {
        return "ProxyCommandImpl{name=$name, permission=$permission, aliases=$aliases, permissionMessage=$permissionMessage, plugin=$plugin}, handle=$handle}"
    }

    companion object Factory {

        fun create (handle: Command, plugin: ProxyPlugin): ProxyCommandImpl {
            return ProxyCommandImpl(handle.name, Optional.ofNullable(handle.permission), handle.aliases,
                Optional.ofNullable(handle.permissionMessage), plugin, handle)
        }

    }

}