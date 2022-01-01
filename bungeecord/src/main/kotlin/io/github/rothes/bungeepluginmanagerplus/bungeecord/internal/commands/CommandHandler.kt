package io.github.rothes.bungeepluginmanagerplus.bungeecord.internal.commands

import io.github.rothes.bungeepluginmanagerplus.api.HandleResult
import io.github.rothes.bungeepluginmanagerplus.bungeecord.api.HandleResultImpl
import io.github.rothes.bungeepluginmanagerplus.bungeecord.internal.I18nHelper
import io.github.rothes.bungeepluginmanagerplus.bungeecord.internal.PluginManager
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.plugin.Command
import net.md_5.bungee.api.plugin.TabExecutor
import java.util.*

object CommandHandler : Command("bungeepluginmanagerplus", "bungeepluginmanagerplus.admin", "bpmp"), TabExecutor {

    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (args.isNotEmpty()) {
            when (args[0].uppercase(Locale.ROOT)) {
                "LOAD" -> {
                    if (args.size == 2) {
                        sendResult(sender, PluginManager.loadPlugin(args[1]))
                        return
                    }
                }
                "UNLOAD" -> {
                    if (args.size == 2) {
                        sendResult(sender, PluginManager.unloadPlugin(args[1]))
                        return
                    }
                }
                "RELOAD" -> {
                    if (args.size == 2) {
                        sendResult(sender, PluginManager.reloadPlugin(args[1]))
                        return
                    }
                }
            }
        }
        sendHelp(sender)
    }

    private fun sendResult(sender: CommandSender, result: HandleResult) {
        if (result is HandleResultImpl)
            result.sendResult(sender)
    }

    fun sendHelp(sender: CommandSender) {
        // Hard code since there's a bug with chat component.
        sender.sendMessage(*ComponentBuilder().appendLegacy("§7§m------").appendLegacy("").strikethrough(false).appendLegacy("§l §7[ §6§lBungeePluginManager§3§l+§7 ]§l ").appendLegacy("§7§m------").create())
        sender.sendMessage(*ComponentBuilder().appendLegacy(I18nHelper.getLocaleMessage("Sender.Commands.Help.Load")).create())
        sender.sendMessage(*ComponentBuilder().appendLegacy(I18nHelper.getLocaleMessage("Sender.Commands.Help.Unload")).create())
        sender.sendMessage(*ComponentBuilder().appendLegacy(I18nHelper.getLocaleMessage("Sender.Commands.Help.Reload")).create())
        sender.sendMessage(*ComponentBuilder().appendLegacy(I18nHelper.getLocaleMessage("Sender.Commands.Help.Footer")).create())
    }

    override fun onTabComplete(sender: CommandSender, args: Array<String>): MutableIterable<String> {
        val list = mutableListOf<String>()
        when (args.size) {
            1 -> {
                list.add("load")
                list.add("unload")
                list.add("reload")
            }
            2 -> {
                when (args[0].uppercase(Locale.ROOT)) {
                    "LOAD"             -> {
                        for (file in ProxyServer.getInstance().pluginsFolder.listFiles()!!) {
                            if (file.isFile && file.name.endsWith(".jar", true))
                                list.add(file.nameWithoutExtension)
                        }
                    }
                    "UNLOAD", "RELOAD" -> {
                        for (plugin in ProxyServer.getInstance().pluginManager.plugins) {
                            if (!plugin.file.parentFile.nameWithoutExtension.contentEquals("modules"))
                                list.add(plugin.description.name)
                        }
                    }
                }
            }
        }
        return list
    }
}