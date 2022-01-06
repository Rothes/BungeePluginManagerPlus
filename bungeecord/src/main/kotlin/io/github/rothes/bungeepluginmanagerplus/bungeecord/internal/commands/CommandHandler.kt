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

    override fun execute(sender: CommandSender, args: Array<String>) {
        if (!sender.hasPermission("bungeepluginmanagerplus.admin")) {
            sender.sendMessage(*ComponentBuilder()
                .appendLegacy(I18nHelper.getPrefixedLocaleMessage("Sender.No-Permission")).create())
            return
        }
        if (args.size > 2) {
            for (i in 2 until args.size) {
                args[1] += " ${args[i]}"
            }
        }
        if (args.isNotEmpty()) {
            when (args[0].uppercase(Locale.ROOT)) {
                "INFO" -> {
                    if (args.size >= 2) {
                        val plug = PluginManager.getPlugins().firstOrNull { plug ->
                            val it = (plug.handle as Plugin).description
                            it.name.contentEquals(args[1], true)
                        } ?: return sender.sendMessage(I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Info.Plugin-Not-Found"))

                        with((plug.handle as Plugin).description) {
                            if (name != null)
                                sender.sendMessage(I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Info.Plugin-Name", name))
                            if (version != null)
                                sender.sendMessage(I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Info.Plugin-Version", version))
                            if (author != null)
                                sender.sendMessage(I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Info.Plugin-Author", author))
                            if (description != null)
                                sender.sendMessage(I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Info.Plugin-Description", description))
                            if (depends != null && depends.isNotEmpty())
                                sender.sendMessage(I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Info.Plugin-Depends", depends.toString()))
                            if (softDepends != null && softDepends.isNotEmpty())
                                sender.sendMessage(I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Info.Plugin-Soft-Depends", softDepends.toString()))
                            if (libraries != null && libraries.isNotEmpty())
                                sender.sendMessage(I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Info.Plugin-Libraries", libraries.toString()))
                            val commands = PluginManager.getCommandsByPlugin(plug)
                            if (commands.isNotEmpty()) {
                                val str = mutableListOf<String>()
                                for (cmd in commands)
                                    str.add(cmd.name)
                                sender.sendMessage(I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Info.Plugin-Commands", str.toString()))
                            }
                            if (file != null)
                                sender.sendMessage(I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Info.Plugin-File", file.path))
                        }
                        return
                    }
                }
                "LIST" -> {
                    if (args.size == 1) {
                        @Suppress("UNCHECKED_CAST")
                        val plugins = mutableListOf<String>()
                        val modules = mutableListOf<String>()
                        for (plugin in PluginManager.getPlugins()) {
                            val des = (plugin.handle as Plugin).description
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
                "LOAD" -> {
                    if (args.size >= 2) {
                        sendResult(sender, PluginManager.loadPlugin(args[1]))
                        return
                    }
                }
                "UNLOAD" -> {
                    if (args.size >= 2) {
                        sendResult(sender, PluginManager.unloadPlugin(args[1]))
                        return
                    }
                }
                "RELOAD" -> {
                    if (args.size >= 2) {
                        sendResult(sender, PluginManager.reloadPlugin(args[1]))
                        return
                    }
                }
                "DISABLE" -> {
                    if (args.size >= 2) {
                        sendResult(sender, PluginManager.disablePlugin(args[1]))
                        return
                    }
                }
                "ENABLE" -> {
                    if (args.size >= 2) {
                        sendResult(sender, PluginManager.enablePlugin(args[1]))
                        return
                    }
                }
                "UPDATE" -> {
                    if (args.size >= 2) {
                        sendResult(sender, PluginManager.updatePlugin(args[1]))
                        return
                    }
                }
                "COMMANDLIST", "CL" -> {
                    if (args.size == 1) {
                        val names = mutableListOf<String>()
                        for (command in PluginManager.getCommandsAll()) {
                            names.add(command.name)
                        }
                        val builder = StringBuilder(I18nHelper.getLocaleMessage("Sender.Commands.Command-List.Message-Format"
                            , names.size.toString()) + "§f: ")
                        for (name in names) {
                            builder.append("§a${name}§f, ")
                        }
                        sender.sendMessage(*ComponentBuilder().appendLegacy(I18nHelper.getLocaleMessage("Sender.Prefix"))
                            .appendLegacy("§r").strikethrough(false).bold(false)
                            .appendLegacy(builder.toString().substring(0, builder.length - 2) + ".").create())
                        return
                    }
                }
                "COMMANDINFO", "CI" -> {
                    if (args.size >= 2) {
                        val command = PluginManager.getCommandByName(args[1])
                        if (command == null) {
                            sender.sendMessage(I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Command-Info.Command-Not-Found"))
                            return
                        }
                        with(command) {
                            sender.sendMessage(I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Command-Info.Command-Name", name))
                            val cmdPerm = this.permission
                            if (cmdPerm != null && cmdPerm.isNotEmpty())
                                sender.sendMessage(I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Command-Info.Command-Permission", cmdPerm))
                            if (aliases.isNotEmpty())
                                sender.sendMessage(I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Command-Info.Command-Aliases",
                                    aliases.contentToString()))
                            sender.sendMessage(I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Command-Info.Command-Plugin", plugin.name))
                        }
                        return
                    }
                }
                "COMMANDREMOVE", "CR" -> {
                    if (args.size >= 2) {
                        val command = PluginManager.getCommandByName(args[1])
                        if (command == null) {
                            sender.sendMessage(I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Command-Remove.Command-Not-Found"))
                            return
                        }
                        sendResult(sender, PluginManager.removeCommand(command))
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
        sender.sendMessage(*ComponentBuilder().appendLegacy(I18nHelper.getLocaleMessage("Sender.Commands.Help.Info")).create())
        sender.sendMessage(*ComponentBuilder().appendLegacy(I18nHelper.getLocaleMessage("Sender.Commands.Help.Load")).create())
        sender.sendMessage(*ComponentBuilder().appendLegacy(I18nHelper.getLocaleMessage("Sender.Commands.Help.Unload")).create())
        sender.sendMessage(*ComponentBuilder().appendLegacy(I18nHelper.getLocaleMessage("Sender.Commands.Help.Reload")).create())
        sender.sendMessage(*ComponentBuilder().appendLegacy(I18nHelper.getLocaleMessage("Sender.Commands.Help.Enable")).create())
        sender.sendMessage(*ComponentBuilder().appendLegacy(I18nHelper.getLocaleMessage("Sender.Commands.Help.Disable")).create())
        sender.sendMessage(*ComponentBuilder().appendLegacy(I18nHelper.getLocaleMessage("Sender.Commands.Help.Update")).create())
        sender.sendMessage(*ComponentBuilder().appendLegacy(I18nHelper.getLocaleMessage("Sender.Commands.Help.Command-List")).create())
        sender.sendMessage(*ComponentBuilder().appendLegacy(I18nHelper.getLocaleMessage("Sender.Commands.Help.Command-Info")).create())
        sender.sendMessage(*ComponentBuilder().appendLegacy(I18nHelper.getLocaleMessage("Sender.Commands.Help.Command-Remove")).create())
        sender.sendMessage(*ComponentBuilder().appendLegacy(I18nHelper.getLocaleMessage("Sender.Commands.Help.Footer")).create())
    }

    override fun onTabComplete(sender: CommandSender, args: Array<String>): MutableIterable<String> {
        if (args.size > 2) {
            for (i in 2 until args.size) {
                args[1] += " ${args[i]}"
            }
        }
        val list = mutableListOf<String>()
        when (args.size) {
            1 -> {
                list.add("info")
                list.add("list")
                list.add("load")
                list.add("unload")
                list.add("reload")
                list.add("enable")
                list.add("disable")
                list.add("update")
                list.add("commandlist")
                list.add("cl")
                list.add("commandinfo")
                list.add("ci")
                list.add("commandremove")
                list.add("cr")
            }
            2 -> {
                when (args[0].uppercase(Locale.ROOT)) {
                    "LOAD"      -> {
                        for (file in ProxyServer.getInstance().pluginsFolder.listFiles()!!) {
                            if (file.isFile && file.name.endsWith(".jar", true))
                                list.add(file.nameWithoutExtension)
                        }
                    }
                    "ENABLE"    -> {
                        for (file in ProxyServer.getInstance().pluginsFolder.listFiles()!!) {
                            if (file.isFile && file.name.endsWith(".disabled", true))
                                list.add(file.nameWithoutExtension)
                        }
                    }
                    "INFO", "UNLOAD", "RELOAD", "DISABLE", "UPDATE" -> {
                        for (plugin in ProxyServer.getInstance().pluginManager.plugins) {
                            if (!plugin.file.parentFile.nameWithoutExtension.contentEquals("modules"))
                                list.add(plugin.description.name)
                        }
                        for (plugin in ProxyServer.getInstance().pluginManager.plugins) {
                            if (!plugin.file.parentFile.nameWithoutExtension.contentEquals("modules"))
                                list.add(plugin.description.name)
                        }
                    }
                    "COMMANDINFO", "CI", "COMMANDREMOVE", "CR"      -> {
                        for (command in PluginManager.getCommandsAll()) {
                            list.add(command.name)
                        }
                    }
                }
            }
        }
        return list
    }
}