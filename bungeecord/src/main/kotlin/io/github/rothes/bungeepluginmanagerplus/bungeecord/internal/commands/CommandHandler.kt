package io.github.rothes.bungeepluginmanagerplus.bungeecord.internal.commands

import io.github.rothes.bungeepluginmanagerplus.api.HandleResult
import io.github.rothes.bungeepluginmanagerplus.bungeecord.api.HandleResultImpl
import io.github.rothes.bungeepluginmanagerplus.bungeecord.internal.I18nHelper
import io.github.rothes.bungeepluginmanagerplus.bungeecord.internal.PluginManager
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.plugin.Command
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.api.plugin.TabExecutor
import java.util.*

object CommandHandler : Command("bungeepluginmanagerplus", null, "bpmp"), TabExecutor {

    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (!sender.hasPermission("bungeepluginmanagerplus.admin")) {
            sender.sendMessage(*ComponentBuilder()
                .appendLegacy(I18nHelper.getPrefixedLocaleMessage("Sender.No-Permission")).create())
            return
        }
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
                "LIST" -> {
                    if (args.size == 1) {
                        @Suppress("UNCHECKED_CAST")
                        val plugins = mutableListOf<String>()
                        val modules = mutableListOf<String>()
                        for (plugin in PluginManager.getPlugins()) {
                            val des = (plugin.instance as Plugin).description
                            if (des.file.startsWith("modules"))
                                modules.add(des.name)
                            else
                                plugins.add(des.name)
                        }
                        val builder = StringBuilder(I18nHelper.getLocaleMessage("Sender.Commands.List.Message-Format"
                            , "§2${plugins.size}${if (modules.size > 0) " §a+ §e${modules.size}" else ""}")
                                + "§f: ")
                        for (plugin in plugins) {
                            builder.append("§a${plugin}§f, ")
                        }
                        for (module in modules) {
                            builder.append("§e${module}§f, ")
                        }
                        sender.sendMessage(*ComponentBuilder().appendLegacy(I18nHelper.getLocaleMessage("Sender.Prefix"))
                            .appendLegacy("§r").strikethrough(false).bold(false)
                            .appendLegacy(builder.toString().substring(0, builder.length - 2) + ".").create())
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

    private fun sendHelp(sender: CommandSender) {
        // Hard code since there's a bug with chat component.
        sender.sendMessage(*ComponentBuilder().appendLegacy("§7§m------").appendLegacy("").strikethrough(false)
            .appendLegacy("§l §7[ §6§lBungeePluginManager§3§l+§7 ]§l ").appendLegacy("§7§m------").create())
        sender.sendMessage(*ComponentBuilder().appendLegacy(I18nHelper.getLocaleMessage("Sender.Commands.Help.List")).create())
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
                list.add("list")
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