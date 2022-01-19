package io.github.rothes.bungeepluginmanagerplus.bungeecord.internal

import io.github.rothes.bungeepluginmanagerplus.bungeecord.BungeePluginManagerPlus
import net.md_5.bungee.api.CommandSender

internal val plugin: BungeePluginManagerPlus by lazy {
    BungeePluginManagerPlus.API as BungeePluginManagerPlus
}

internal fun info(msg: String) {
    plugin.logger.info(msg)
}

internal fun warn(msg: String) {
    plugin.logger.warning(msg)
}

internal fun error(msg: String) {
    plugin.logger.severe(msg)
}

@Suppress("DEPRECATION")
fun CommandSender.messageLocaled(key: String, vararg replacements: String, prefixed: Boolean = true) {
    if (prefixed)
        sendMessage(I18nHelper.getPrefixedLocaleMessage(key, replacements))
    else
        sendMessage(I18nHelper.getLocaleMessage(key, replacements))
}