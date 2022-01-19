package io.github.rothes.bungeepluginmanagerplus.bungeecord.internal.commands

import io.github.rothes.bungeepluginmanagerplus.api.HandleResult
import io.github.rothes.bungeepluginmanagerplus.bungeecord.api.HandleResultImpl
import io.github.rothes.bungeepluginmanagerplus.bungeecord.internal.I18nHelper
import io.github.rothes.bungeepluginmanagerplus.bungeecord.internal.PluginManager
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Command
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.api.plugin.TabExecutor
import java.util.*
import java.util.regex.Pattern

object CommandHandler : Command("bungeepluginmanagerplus", null, "bpmp"), TabExecutor {

    override fun execute(sender: CommandSender, oriArgs: Array<String>) {
        if (!sender.hasPermission("bungeepluginmanagerplus.admin")) {
            sender.messageLocaled("Sender.No-Permission")
            return
        }
        val args = mergeQuotes(oriArgs)
        if (args.isNotEmpty()) {
            when (args[0].uppercase(Locale.ROOT)) {
                "HELP", "?" -> {
                    if (args.size == 2) {
                        sendHelp(sender, args[1])
                        return
                    }
                }
                "INFO" -> {
                    if (args.size >= 2) {
                        val plug = PluginManager.getPlugins().firstOrNull { plug ->
                            val it = (plug.handle as Plugin).description
                            it.name.contentEquals(args[1], true)
                        } ?: return sender.messageLocaled("Sender.Commands.Info.Plugin-Not-Found")

                        with((plug.handle as Plugin).description) {
                            if (name != null)
                                sender.messageLocaled("Sender.Commands.Info.Plugin-Name", name)
                            if (version != null)
                                sender.messageLocaled("Sender.Commands.Info.Plugin-Version", version)
                            if (author != null)
                                sender.messageLocaled("Sender.Commands.Info.Plugin-Author", author)
                            if (description != null)
                                sender.messageLocaled("Sender.Commands.Info.Plugin-Description", description)
                            if (depends != null && depends.isNotEmpty())
                                sender.messageLocaled("Sender.Commands.Info.Plugin-Depends", depends.toString())
                            if (softDepends != null && softDepends.isNotEmpty())
                                sender.messageLocaled("Sender.Commands.Info.Plugin-Soft-Depends", softDepends.toString())
                            if (libraries != null && libraries.isNotEmpty())
                                sender.messageLocaled("Sender.Commands.Info.Plugin-Libraries", libraries.toString())
                            val commands = PluginManager.getCommandsByPlugin(plug)
                            if (commands.isNotEmpty()) {
                                val str = mutableListOf<String>()
                                for (cmd in commands)
                                    str.add(cmd.name)
                                sender.messageLocaled("Sender.Commands.Info.Plugin-Commands", str.toString())
                            }
                            /*
                            val eventHandlers = PluginManager.getEventHandlersByPlugin(plug)
                            if (eventHandlers.isNotEmpty()) {
                                val str = mutableListOf<String>()
                                for (handler in eventHandlers)
                                    str.add("§e(${handler.event.name})§f${handler.method.declaringClass.name}§e:§f${handler.method.name}")
                                sender.messageLocaled("Sender.Commands.Info.Plugin-Event-Handlers", str.toString())
                            }*/ // Too long, use Listeners instead.
                            val listeners = PluginManager.getEventListenersByPlugin(plug)
                            if (listeners.isNotEmpty()) {
                                val str = mutableListOf<String>()
                                for (listener in listeners)
                                    str.add("§e(${PluginManager.getEventHandlersByListener(listener).size})§f${listener.clazz.name}")
                                sender.messageLocaled("Sender.Commands.Info.Plugin-Event-Listeners", str.toString())
                            }
                            if (file != null)
                                sender.messageLocaled("Sender.Commands.Info.Plugin-File", file.path)
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
                        @Suppress("DEPRECATION") sender.sendMessage(I18nHelper.getLocaleMessage("Sender.Prefix")
                                + builder.toString().substring(0, builder.length - 2) + ".")
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
                        @Suppress("DEPRECATION") sender.sendMessage(I18nHelper.getLocaleMessage("Sender.Prefix")
                                + builder.toString().substring(0, builder.length - 2) + ".")
                        return
                    }
                }
                "COMMANDINFO", "CI" -> {
                    if (args.size >= 2) {
                        val command = PluginManager.getCommandByName(args[1])
                        if (command == null) {
                            sender.messageLocaled("Sender.Commands.Command-Info.Command-Not-Found")
                            return
                        }
                        with(command) {
                            sender.messageLocaled("Sender.Commands.Command-Info.Command-Name", name)
                            val cmdPerm = this.permission
                            if (cmdPerm != null && cmdPerm.isNotEmpty())
                                sender.messageLocaled("Sender.Commands.Command-Info.Command-Permission", cmdPerm)
                            if (aliases.isNotEmpty())
                                sender.messageLocaled("Sender.Commands.Command-Info.Command-Aliases",
                                    aliases.contentToString())
                            sender.messageLocaled("Sender.Commands.Command-Info.Command-Plugin", plugin.name)
                        }
                        return
                    }
                }
                "COMMANDREMOVE", "CR" -> {
                    if (args.size >= 2) {
                        val command = PluginManager.getCommandByName(args[1])
                        if (command == null) {
                            sender.messageLocaled("Sender.Commands.Command-Remove.Command-Not-Found")
                            return
                        }
                        sendResult(sender, PluginManager.removeCommand(command))
                        return
                    }
                }
                "EVENTLISTENERLIST", "ELL" -> {
                    if (args.size == 1) {
                        val names = mutableListOf<String>()
                        for (listener in PluginManager.getEventListenersAll()) {
                            names.add("§e(${PluginManager.getEventHandlersByListener(listener).size})§f${listener.clazz.name}")
                        }
                        val builder = StringBuilder(I18nHelper.getLocaleMessage("Sender.Commands.Event-Listener-List.Message-Format"
                            , names.size.toString()) + "§f: ")
                        names.forEach { builder.append("§a${it}§f, ") }
                        @Suppress("DEPRECATION") sender.sendMessage(I18nHelper.getLocaleMessage("Sender.Prefix")
                                + builder.toString().substring(0, builder.length - 2) + ".")
                        return
                    }
                }
                "EVENTLISTENERINFO", "ELI" -> {
                    if (args.size == 2) {
                        val listener = PluginManager.getEventListenersAll().firstOrNull {
                            it.clazz.name == args[1]
                        } ?: return sender.messageLocaled("Sender.Commands.Event-Listener-Info.Event-Listener-Not-Found")

                        val names = mutableListOf<String>()
                        for (handler in PluginManager.getEventHandlersByListener(listener)) {
                            names.add("§e(${handler.event.name})§f${handler.method.declaringClass.name}§e:§f${handler.method.name}")
                        }

                        sender.messageLocaled("Sender.Commands.Event-Listener-Info.Event-Listener-Class", listener.clazz.name)
                        sender.messageLocaled("Sender.Commands.Event-Listener-Info.Event-Listener-Handlers", names.toString())
                        sender.messageLocaled("Sender.Commands.Event-Listener-Info.Event-Listener-Plugin", listener.plugin.name)
                        return
                    }
                }
                "EVENTLISTENERREMOVE", "ELR" -> {
                    if (args.size == 2) {
                        val listener = PluginManager.getEventListenersAll().firstOrNull {
                            it.clazz.name == args[1]
                        } ?: return sender.messageLocaled("Sender.Commands.Event-Listener-Remove.Event-Listener-Not-Found")

                        sendResult(sender, PluginManager.removeEventListener(listener))
                        return
                    }
                }
                "EVENTHANDLERLIST", "EHL" -> {
                    if (args.size == 1) {
                        val names = mutableListOf<String>()
                        for (handler in PluginManager.getEventHandlersAll()) {
                            names.add("§e(${handler.event.name})§f${handler.method.declaringClass.name}§e:§f${handler.method.name}")
                        }
                        val builder = StringBuilder(I18nHelper.getLocaleMessage("Sender.Commands.Event-Handler-List.Message-Format"
                            , names.size.toString()) + "§f: ")
                        names.forEach { builder.append("§a${it}§f, ") }
                        @Suppress("DEPRECATION") sender.sendMessage(I18nHelper.getLocaleMessage("Sender.Prefix")
                                + builder.toString().substring(0, builder.length - 2) + ".")
                        return
                    }
                }
                "EVENTHANDLERINFO", "EHI" -> {
                    if (args.size == 3) {
                        val handler = PluginManager.getEventHandlersAll().firstOrNull {
                            it.method.declaringClass.name == args[1] && it.method.name == args[2]
                        } ?: return sender.messageLocaled("Sender.Commands.Event-Handler-Info.Event-Handler-Not-Found")

                        sender.messageLocaled("Sender.Commands.Event-Handler-Info.Event-Handler-Event", handler.event.name)
                        sender.messageLocaled("Sender.Commands.Event-Handler-Info.Event-Handler-Priority", handler.priority.name)
                        sender.messageLocaled("Sender.Commands.Event-Handler-Info.Event-Handler-Class", handler.method.declaringClass.name)
                        sender.messageLocaled("Sender.Commands.Event-Handler-Info.Event-Handler-Method", handler.method.name)
                        sender.messageLocaled("Sender.Commands.Event-Handler-Info.Event-Handler-Plugin", handler.plugin.name)
                        return
                    }
                }
                "EVENTHANDLERREMOVE", "EHR" -> {
                    if (args.size == 3) {
                        val handler = PluginManager.getEventHandlersAll().firstOrNull {
                            it.method.declaringClass.name == args[1] && it.method.name == args[2]
                        } ?: return sender.messageLocaled("Sender.Commands.Event-Handler-Remove.Event-Handler-Not-Found")

                        sendResult(sender, PluginManager.removeEventHandler(handler))
                        return
                    }
                }
            }
        }
        sendHelp(sender, "1")
    }

    private fun sendResult(sender: CommandSender, result: HandleResult) {
        if (result is HandleResultImpl)
            result.sendResult(sender)
    }

    private fun sendHelp(sender: CommandSender, page: String) {
        // Hard code since there's a bug with chat component.
        sender.messageLocaled("Sender.Commands.Help.Header", prefixed = false)
        when (page) {
            "1" -> {
                sender.messageLocaled("Sender.Commands.Help.List", prefixed = false)
                sender.messageLocaled("Sender.Commands.Help.Info", prefixed = false)
                sender.messageLocaled("Sender.Commands.Help.Load", prefixed = false)
                sender.messageLocaled("Sender.Commands.Help.Unload", prefixed = false)
                sender.messageLocaled("Sender.Commands.Help.Reload", prefixed = false)
                sender.messageLocaled("Sender.Commands.Help.Enable", prefixed = false)
                sender.messageLocaled("Sender.Commands.Help.Disable", prefixed = false)
                sender.messageLocaled("Sender.Commands.Help.Update", prefixed = false)
                sender.messageLocaled("Sender.Commands.Help.Next-Page", "2", prefixed = false)
            }
            "2" -> {
                sender.messageLocaled("Sender.Commands.Help.Command-List", prefixed = false)
                sender.messageLocaled("Sender.Commands.Help.Command-Info", prefixed = false)
                sender.messageLocaled("Sender.Commands.Help.Command-Remove", prefixed = false)
                sender.messageLocaled("Sender.Commands.Help.Event-Listener-List", prefixed = false)
                sender.messageLocaled("Sender.Commands.Help.Event-Listener-Info", prefixed = false)
                sender.messageLocaled("Sender.Commands.Help.Event-Listener-Remove", prefixed = false)
                sender.messageLocaled("Sender.Commands.Help.Event-Handler-List", prefixed = false)
                sender.messageLocaled("Sender.Commands.Help.Event-Handler-Info", prefixed = false)
                sender.messageLocaled("Sender.Commands.Help.Event-Handler-Remove", prefixed = false)
            }
            else -> {
                sender.messageLocaled("Sender.Commands.Help.Page-Exceeded", "2", prefixed = false)
            }
        }
        sender.messageLocaled("Sender.Commands.Help.Footer", prefixed = false)
    }

    override fun onTabComplete(sender: CommandSender, oriArgs: Array<String>): MutableIterable<String> {
        val args = mergeQuotes(oriArgs)
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
                list.add("commandinfo")
                list.add("commandremove")
                list.add("eventlistenerlist")
                list.add("eventlistenerinfo")
                list.add("eventlistenerremove")
                list.add("eventhandlerlist")
                list.add("eventhandlerinfo")
                list.add("eventhandlerremove")
            }
            2 -> {
                when (args[0].uppercase(Locale.ROOT)) {
                    "LOAD"      -> {
                        for (file in ProxyServer.getInstance().pluginsFolder.listFiles()!!) {
                            if (file.isFile && file.name.endsWith(".jar", true))
                                list.add(formatWithQuotes(file.nameWithoutExtension))
                        }
                    }
                    "ENABLE"    -> {
                        for (file in ProxyServer.getInstance().pluginsFolder.listFiles()!!) {
                            if (file.isFile && file.name.endsWith(".disabled", true))
                                list.add(formatWithQuotes(file.nameWithoutExtension))
                        }
                    }
                    "INFO", "UNLOAD", "RELOAD", "DISABLE", "UPDATE" -> {
                        for (plugin in ProxyServer.getInstance().pluginManager.plugins) {
                            if (!plugin.file.parentFile.nameWithoutExtension.contentEquals("modules"))
                                list.add(formatWithQuotes(plugin.description.name))
                        }
                    }
                    "COMMANDINFO", "CI", "COMMANDREMOVE", "CR"      -> {
                        for (command in PluginManager.getCommandsAll()) {
                            list.add(formatWithQuotes(command.name))
                        }
                    }
                    "EVENTLISTENERINFO", "ELI", "EVENTLISTENERREMOVE", "ELR" -> {
                        for (listener in PluginManager.getEventListenersAll()) {
                            list.add(formatWithQuotes(listener.clazz.name))
                        }
                    }
                    "EVENTHANDLERINFO", "EHI", "EVENTHANDLERREMOVE", "EHR" -> {
                        for (handler in PluginManager.getEventHandlersAll()) {
                            list.add(formatWithQuotes(handler.method.declaringClass.name))
                        }
                    }
                }
            }
            3 -> {
                when (args[0].uppercase(Locale.ROOT)) {
                    "EVENTHANDLERINFO", "EHI", "EVENTHANDLERREMOVE", "EHR" -> {
                        for (handler in PluginManager.getEventHandlersAll().filter {
                            args[1] == formatWithQuotes(it.method.declaringClass.name, true)
                                    || args[1] == it.method.declaringClass.name
                        }) {
                            list.add(formatWithQuotes(handler.method.name))
                        }
                    }
                }
            }
        }
        return list
    }


    private val lastQuotes: Pattern = Pattern.compile("\"+$")

    private fun formatWithQuotes(string: String, force: Boolean = false): String {
        return if (!force || (string.isEmpty() || !string.contains(' '))) {
            string
        } else {
            val stringBuilder = StringBuilder()
            stringBuilder.append('"')
            val args = string.split(" ").toTypedArray()
            for (arg in args) {
                stringBuilder.append(" ").append(arg)
                val chars = arg.toCharArray()
                for (i1 in chars.indices.reversed()) {
                    if (chars[i1] == '"') {
                        stringBuilder.append('"')
                    } else {
                        break
                    }
                }
            }
            stringBuilder.deleteCharAt(1).append('"')
            stringBuilder.toString()
        }
    }

    private fun mergeQuotes(strings: Array<String>): Array<String> {
        // -1 if no quote at start was delected.
        var startIndex = -1
        val merged = LinkedList<String>()
        for (i in strings.indices) {
            val arg = strings[i]
            val length = arg.length
            if (startIndex == -1) {
                if (length > 0 && arg[0] == '"') {
                    startIndex = i
                } else {
                    merged.add(arg)
                }
            }
            if (startIndex != -1 && (startIndex != i && length == 1 && arg[0] == '"' || length > 1 && arg[length - 1] == '"')) {
                val matcher = lastQuotes.matcher(strings[i])
                matcher.find()
                val quotes: String = matcher.group(0)
                if (quotes.length % 2 == 1) {
                    val stringBuilder = StringBuilder()
                    stringBuilder.append(strings[startIndex])
                    addArgument(stringBuilder, strings[startIndex++])
                    while (startIndex <= i) {
                        stringBuilder.append(" ").append(strings[startIndex])
                        addArgument(stringBuilder, strings[startIndex++])
                    }
                    stringBuilder.deleteCharAt(0).deleteCharAt(stringBuilder.length - 1)
                    merged.add(stringBuilder.toString())
                    startIndex = -1
                }
            }
        }
        if (startIndex != -1) {
            while (startIndex < strings.size) {
                merged.add(strings[startIndex++])
            }
        }
        return merged.toTypedArray()
    }

    private fun addArgument(stringBuilder: StringBuilder, arg: String) {
        val matcher = lastQuotes.matcher(arg)
        if (matcher.find()) {
            val quotes = matcher.group(0)
            val length = stringBuilder.length
            stringBuilder.delete(length - quotes.length / 2, length)
        }
    }

    @Suppress("DEPRECATION")
    private fun CommandSender.messageLocaled(key: String, vararg replacements: String, prefixed: Boolean = true) {
        if (prefixed)
            sendMessage(I18nHelper.getPrefixedLocaleMessage(key, replacements))
        else
            sendMessage(I18nHelper.getLocaleMessage(key, replacements))
    }

}