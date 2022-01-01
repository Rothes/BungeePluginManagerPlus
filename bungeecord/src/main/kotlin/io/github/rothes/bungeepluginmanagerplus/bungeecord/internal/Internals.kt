package io.github.rothes.bungeepluginmanagerplus.bungeecord.internal

import io.github.rothes.bungeepluginmanagerplus.bungeecord.BungeePluginManagerPlus

internal val plugin: BungeePluginManagerPlus by lazy {
    BungeePluginManagerPlus.API as BungeePluginManagerPlus
}

internal fun log(msg: String) {
    plugin.logger.info(msg)
}

internal fun warn(msg: String) {
    plugin.logger.warning(msg)
}

internal fun error(msg: String) {
    plugin.logger.severe(msg)
}