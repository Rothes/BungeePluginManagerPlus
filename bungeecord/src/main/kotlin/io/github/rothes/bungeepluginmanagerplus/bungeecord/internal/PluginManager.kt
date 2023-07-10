package io.github.rothes.bungeepluginmanagerplus.bungeecord.internal

import com.google.common.collect.Multimap
import io.github.rothes.bungeepluginmanagerplus.api.Action
import io.github.rothes.bungeepluginmanagerplus.api.HandleResult
import io.github.rothes.bungeepluginmanagerplus.api.ProxyCommand
import io.github.rothes.bungeepluginmanagerplus.api.ProxyEventHandler
import io.github.rothes.bungeepluginmanagerplus.api.ProxyEventListener
import io.github.rothes.bungeepluginmanagerplus.api.ProxyPlugin
import io.github.rothes.bungeepluginmanagerplus.bungeecord.api.HandleResultImpl
import io.github.rothes.bungeepluginmanagerplus.bungeecord.api.ProxyCommandImpl
import io.github.rothes.bungeepluginmanagerplus.bungeecord.api.ProxyEventHandlerImpl
import io.github.rothes.bungeepluginmanagerplus.bungeecord.api.ProxyEventImpl
import io.github.rothes.bungeepluginmanagerplus.bungeecord.api.ProxyEventListenerImpl
import io.github.rothes.bungeepluginmanagerplus.bungeecord.api.ProxyPluginImpl
import io.github.rothes.bungeepluginmanagerplus.bungeecord.events.AbstractBpmpEvent
import io.github.rothes.bungeepluginmanagerplus.bungeecord.events.EventFactory
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Command
import net.md_5.bungee.api.plugin.Event
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.api.plugin.PluginDescription
import net.md_5.bungee.event.EventBus
import net.md_5.bungee.event.EventHandlerMethod
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import java.io.File
import java.lang.reflect.Method
import java.nio.file.Files
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.jar.JarFile
import net.md_5.bungee.api.plugin.PluginManager as BungeePluginManager

object PluginManager {

    private val yaml: Yaml by lazy {
        val yamlConstructor = Constructor(LoaderOptions())
        val propertyUtils = yamlConstructor.propertyUtils
        propertyUtils.isSkipMissingProperties = true
        yamlConstructor.propertyUtils = propertyUtils
        Yaml(yamlConstructor)
    }

    @Suppress("UNCHECKED_CAST")
    private val bcPlugins: MutableMap<String, Plugin> by lazy {
        val field = BungeePluginManager::class.java.getDeclaredField("plugins")
        field.isAccessible = true
        field[ProxyServer.getInstance().pluginManager] as MutableMap<String, Plugin>
    }
    @Suppress("UNCHECKED_CAST")
    private val bcToLoad: MutableMap<String, PluginDescription> by lazy {
        val field = BungeePluginManager::class.java.getDeclaredField("toLoad")
        field.isAccessible = true
        val override = mutableMapOf<String, PluginDescription>()
        for (entry in bcPlugins.entries) {
            override[entry.key] = entry.value.description
        }
        field[ProxyServer.getInstance().pluginManager] = override
        override
    }
    @Suppress("UNCHECKED_CAST")
    private val bcCommandsPlugin: Multimap<Plugin, Command> by lazy {
        val field = BungeePluginManager::class.java.getDeclaredField("commandsByPlugin")
        field.isAccessible = true
        field.get(ProxyServer.getInstance().pluginManager) as Multimap<Plugin, Command>
    }
    @Suppress("UNCHECKED_CAST")
    private val bcCommandsString: MutableMap<String, Command> by lazy {
        val field = BungeePluginManager::class.java.getDeclaredField("commandMap")
        field.isAccessible = true
        field.get(ProxyServer.getInstance().pluginManager) as MutableMap<String, Command>
    }
    @Suppress("UNCHECKED_CAST")
    private val bcListenersPlugin: Multimap<Plugin, Listener> by lazy {
        val field = BungeePluginManager::class.java.getDeclaredField("listenersByPlugin")
        field.isAccessible = true
        field.get(ProxyServer.getInstance().pluginManager) as Multimap<Plugin, Listener>
    }
    @Suppress("UNCHECKED_CAST")
    private val bcListenerPriority: MutableMap<Class<*>, Map<Byte, Map<Any, Array<Method>>>> by lazy {
        val managerField = BungeePluginManager::class.java.getDeclaredField("eventBus")
        managerField.isAccessible = true
        val busField = EventBus::class.java.getDeclaredField("byListenerAndPriority")
        busField.isAccessible = true
        busField.get(managerField[ProxyServer.getInstance().pluginManager]) as MutableMap<Class<*>, Map<Byte, Map<Any, Array<Method>>>>
    }
    @Suppress("UNCHECKED_CAST")
    private val bcEventBaked: ConcurrentHashMap<Class<*>, Array<EventHandlerMethod>> by lazy {
        val managerField = BungeePluginManager::class.java.getDeclaredField("eventBus")
        managerField.isAccessible = true
        val busField = EventBus::class.java.getDeclaredField("byEventBaked")
        busField.isAccessible = true
        busField.get(managerField[ProxyServer.getInstance().pluginManager]) as ConcurrentHashMap<Class<*>, Array<EventHandlerMethod>>
    }
    private val enablePluginMethod: Method by lazy {
        val method = BungeePluginManager::class.java.getDeclaredMethod("enablePlugin",
            Map::class.java, Stack::class.java, PluginDescription::class.java)
        method.isAccessible = true
        method
    }


    internal fun getPlugins() : Array<ProxyPlugin> {
        val plugins = ProxyServer.getInstance().pluginManager.plugins
        val list = mutableListOf<ProxyPlugin>()
        for (plugin in plugins) {
            list.add(ProxyPluginImpl.create(plugin))
        }
        return list.also { it ->
            it.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
        }.toTypedArray()
    }

    internal fun loadPlugin(plugin: String, action: Action = Action.PLUGIN_LOAD, eventId: Short = -1): HandleResult {
        val file = searchPlugin(plugin) ?: return HandleResultImpl.create(Action.PLUGIN_LOAD, false,
            I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Load.Plugin-Not-Found"), null)

        return loadPlugin(file, action)
    }

    internal fun loadPlugin(plugin: File, action: Action = Action.PLUGIN_LOAD, eventId: Short = -1): HandleResult {
        if (!plugin.exists()) {
            return HandleResultImpl.create(Action.PLUGIN_LOAD, false,
                I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Load.Invalid-File", plugin.name), null)
        }
        val pluginDes = getPluginDesYaml(plugin) ?: return HandleResultImpl.create(Action.PLUGIN_LOAD, false,
            I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Load.Invalid-Plugin-Description"), null)
        for (depend in pluginDes.depends) {
            ProxyServer.getInstance().pluginManager.getPlugin(depend)
                ?: return HandleResultImpl.create(Action.PLUGIN_LOAD, false,
                    I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Load.Missing-Dependency", depend), null)
        }
        if (bcPlugins.containsKey(pluginDes.name)) return HandleResultImpl.create(Action.PLUGIN_LOAD, false,
            I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Load.Plugin-Already-Loaded"),
            ProxyPluginImpl.create(bcPlugins[pluginDes.name]!!))

        val event = EventFactory.createPluginLoadEvent(action, pluginDes.name, plugin, if (eventId < 0) EventFactory.nextId() else eventId)
        ProxyServer.getInstance().pluginManager.callEvent(event)
        if (event.isCancelled)
            return HandleResultImpl.create(Action.PLUGIN_LOAD, false,
                I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Load.Event-Cancelled",
                    (event.cancelledMessage ?: I18nHelper.getLocaleMessage("Sender.Event.Cancelled-Reasons.Default-Reason"))),
                null)

        return try {
            val success = enablePluginMethod.invoke(ProxyServer.getInstance().pluginManager,
                HashMap<PluginDescription, Boolean>(), Stack<PluginDescription>(), pluginDes) as Boolean

            with (bcPlugins[pluginDes.name]!!) {
                this.onLoad()
                this.onEnable()
            }
            pluginDes.file = plugin
            bcToLoad[pluginDes.name] = pluginDes
            HandleResultImpl.create(Action.PLUGIN_LOAD, success,
                if (success) I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Load.Success-Loaded-Plugin", pluginDes.name)
                else I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Load.Failed-Loading-Plugin", pluginDes.name),
                if (success) ProxyPluginImpl.create(bcPlugins[pluginDes.name]!!) else null)
        } catch (e: Throwable) {
            e.printStackTrace()
            HandleResultImpl.create(Action.PLUGIN_LOAD, false,
                I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Load.Failed-Loading-Plugin", pluginDes.name), null)
        }
    }

    internal fun unloadPlugin(plugin: String, action: Action = Action.PLUGIN_UNLOAD, eventId: Short = -1): HandleResult {
        val instance = bcPlugins[plugin] ?: bcPlugins[bcPlugins.keys.firstOrNull { it.equals(plugin, true) }
        ] ?: return HandleResultImpl.create(Action.PLUGIN_UNLOAD, false,
            I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Unload.Plugin-Not-Exist", plugin), null)

        val event = EventFactory.createPluginUnloadEvent(action, ProxyPluginImpl.create(instance), if (eventId < 0) EventFactory.nextId() else eventId)
        ProxyServer.getInstance().pluginManager.callEvent(event)
        if (event.isCancelled)
            return HandleResultImpl.create(Action.PLUGIN_UNLOAD, false,
                I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Unload.Event-Cancelled", getEventCancelledMessage(event)),
                ProxyPluginImpl.create(instance))

        return try {
            ProxyServer.getInstance().pluginManager.unregisterCommands(instance)
            ProxyServer.getInstance().pluginManager.unregisterListeners(instance)
            ProxyServer.getInstance().scheduler.cancel(instance)
            instance.onDisable()
            bcPlugins.remove(instance.description.name)
            bcToLoad.remove(instance.description.name)
            HandleResultImpl.create(Action.PLUGIN_UNLOAD, true,
                I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Unload.Success-Unloaded-Plugin", instance.description.name),
                ProxyPluginImpl.create(instance))
        } catch (e: Throwable) {
            HandleResultImpl.create(Action.PLUGIN_UNLOAD, false,
                I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Unload.Failed-Unloading-Plugin", instance.description.name),
                ProxyPluginImpl.create(instance))
        }
    }

    internal fun reloadPlugin(plugin: String): HandleResult {
        val eventId = EventFactory.nextId()
        val unload = unloadPlugin(plugin, Action.PLUGIN_RELOAD, eventId)
        if (!unload.success)
            return HandleResultImpl.create(Action.PLUGIN_RELOAD, false, unload.message, unload.plugin)
        val load = loadPlugin((unload.plugin.get().handle as Plugin).description.file, Action.PLUGIN_RELOAD, eventId)
        if (!load.success)
            return HandleResultImpl.create(Action.PLUGIN_RELOAD, false, load.message, load.plugin)
        return HandleResultImpl.create(Action.PLUGIN_RELOAD, true,
            I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Reload.Success-Reloaded-Plugin", plugin), load.plugin)
    }

    internal fun enablePlugin(plugin: String): HandleResult {
        val folder = ProxyServer.getInstance().pluginsFolder
        val found = listFiles(folder).firstOrNull {
            it.name.endsWith(".disabled", true) && it.nameWithoutExtension.contentEquals(plugin, true)
        // Also in modules folder
        } ?: listFiles(File(folder.parentFile, "modules")).firstOrNull {
            it.name.endsWith(".disabled", true) && it.nameWithoutExtension.contentEquals(plugin, true)
        } ?: return HandleResultImpl.create(Action.PLUGIN_ENABLE, false
            , I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Enable.Plugin-Not-Found"), null)

        val load = loadPlugin(enableFile(found), Action.PLUGIN_ENABLE)
        if (load.success)
            found.renameTo(File(found.parentFile, "${found.nameWithoutExtension}.jar"))
        return HandleResultImpl.create(Action.PLUGIN_ENABLE, load.success, if (load.success)
            I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Enable.Success-Enabled-Plugin",
                (load.plugin.get().handle as Plugin).description.name) else load.message,
            load.plugin)
    }

    internal fun disablePlugin(plugin: String): HandleResult {
        val unload = unloadPlugin(plugin, Action.PLUGIN_DISABLE)
        return if (unload.success) {
            val file = (unload.plugin.get().handle as Plugin).description.file
            disableFile(file)
//            warn(file.renameTo(File(file.parentFile, file.nameWithoutExtension + ".jar.disabled")).toString())
            HandleResultImpl.create(Action.PLUGIN_DISABLE, true,
                I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Disable.Success-Disabled-Plugin",
                    (unload.plugin.get().handle as Plugin).description.name), unload.plugin)
        } else {
            HandleResultImpl.create(Action.PLUGIN_DISABLE, false, unload.message, unload.plugin)
        }
    }

    internal fun updatePlugin(plugin: String): HandleResult {
        val instance = bcPlugins.values.firstOrNull {
            it.description.name.equals(plugin, true)
        } ?: return HandleResultImpl.create(Action.PLUGIN_UPDATE, false,
            I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Update.Plugin-Not-Found"), null)
        val update = listFiles(instance.file.parentFile).firstOrNull {
            if (isPluginJar(it)) {
                val des = getPluginDesYaml(it)
                des != null && des.version != instance.description.version && des.name == instance.description.name
            } else false
        } ?: return HandleResultImpl.create(Action.PLUGIN_UPDATE, false,
            I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Update.Update-Not-Found"),
            ProxyPluginImpl.create(instance))

        val eventId = EventFactory.nextId()
        val oldFile = instance.description.file
        val unload = unloadPlugin(instance.description.name, Action.PLUGIN_UPDATE, eventId)
        if (!unload.success) {
            return HandleResultImpl.create(Action.PLUGIN_UPDATE, false, unload.message,
                ProxyPluginImpl.create(instance))
        }

        val load = loadPlugin(update, Action.PLUGIN_UPDATE, eventId)
        if (!load.success) {
            return HandleResultImpl.create(Action.PLUGIN_UPDATE, false,
                load.message + I18nHelper.getLocaleMessage("Sender.Commands.Update.Reverted-Old-Version"),
                loadPlugin(oldFile).plugin)
        }
        oldFile(oldFile)
        return HandleResultImpl.create(Action.PLUGIN_UPDATE, true
            , I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Update.Success-Updated-Plugin"
                , instance.description.name, instance.description.version
                , (load.plugin.get().handle as Plugin).description.version), load.plugin)
    }

    internal fun getCommandByName(command: String): ProxyCommand? {
        return getCommandsAll().firstOrNull {
            it.name.contentEquals(command, true)
        }
    }

    internal fun getCommandsByPlugin(plugin: ProxyPlugin): Array<ProxyCommand> {
        val result = mutableListOf<ProxyCommand>()
        for (command in bcCommandsPlugin[plugin.handle as Plugin]) {
            result.add(ProxyCommandImpl.create(command, plugin))
        }
        return result.toTypedArray()
    }

    internal fun getCommandsAll(): Array<ProxyCommand> {
        val result = mutableListOf<ProxyCommand>()
        for (entry in bcCommandsPlugin.entries()) {
            if (entry.key == null)
                continue
            result.add(ProxyCommandImpl.create(entry.value, ProxyPluginImpl.create(entry.key)))
        }
        return result.toTypedArray()
    }

    internal fun removeCommand(command: ProxyCommand): HandleResult {
        val event = EventFactory.createPluginCommandRemoveEvent(command)
        ProxyServer.getInstance().pluginManager.callEvent(event)
        if (event.isCancelled)
            return HandleResultImpl.create(Action.COMMAND_REMOVE, false,
                I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Command-Remove.Event-Cancelled", getEventCancelledMessage(event)),
                Optional.of(command.plugin))

        bcCommandsPlugin[command.plugin.handle as Plugin].remove(command.handle)
        bcCommandsString.remove(command.name.lowercase(Locale.ROOT))
        for (alias in command.aliases) {
            bcCommandsString.remove(alias.lowercase(Locale.ROOT))
        }
        return HandleResultImpl.create(Action.COMMAND_REMOVE, true,
            I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Command-Remove.Success-Removed-Command", command.name),
            Optional.of(command.plugin))
    }

    internal fun getEventListenersAll(): Array<ProxyEventListener> {
        val result = mutableListOf<ProxyEventListener>()
        for ((plugin, listener) in bcListenersPlugin.entries()) {
            result.add(ProxyEventListenerImpl.create(listener, ProxyPluginImpl.create(plugin)))
        }
        return result.toTypedArray()
    }

    internal fun getEventListenersByPlugin(plugin: ProxyPlugin): Array<ProxyEventListener> {
        return getEventListenersAll().filter { it.plugin.handle === plugin.handle }.toTypedArray()
    }

    internal fun removeEventListener(listener: ProxyEventListener): HandleResult {
        val event = EventFactory.createPluginEventListenerRemoveEvent(listener)
        ProxyServer.getInstance().pluginManager.callEvent(event)
        if (event.isCancelled)
            return HandleResultImpl.create(Action.EVENT_LISTENER_REMOVE, false,
                I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Event-Listener-Remove.Event-Cancelled", getEventCancelledMessage(event)),
                Optional.of(listener.plugin))

        ProxyServer.getInstance().pluginManager.unregisterListener(listener.handle as Listener)
        return HandleResultImpl.create(Action.EVENT_LISTENER_REMOVE, true,
            I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Event-Listener-Remove.Success-Removed-Event-Listener"),
            Optional.of(listener.plugin))
    }

    internal fun getEventHandlersAll(): Array<ProxyEventHandler> {
        val result = mutableListOf<ProxyEventHandler>()
        for (eventAndMethods in bcEventBaked) {
            @Suppress("UNCHECKED_CAST")
            val eventClass = eventAndMethods.key as Class<out Event>
            for (method in eventAndMethods.value) {
                val pluginHandler: Plugin = bcListenersPlugin.entries().first { it.value === method.listener }.key
                result.add(ProxyEventHandlerImpl.create(
                    ProxyEventImpl.create(eventClass),
                    ProxyPluginImpl.create(pluginHandler),
                    method))
            }

        }
        return result.toTypedArray()
    }

    internal fun getEventHandlersByPlugin(plugin: ProxyPlugin): Array<ProxyEventHandler> {
        return getEventHandlersAll().filter { it.plugin.handle === plugin.handle }.toTypedArray()
    }

    internal fun getEventHandlersByListener(listener: ProxyEventListener): Array<ProxyEventHandler> {
        return getEventHandlersAll().filter { it.method.declaringClass === listener.clazz }.toTypedArray()
    }

    internal fun removeEventHandler(handler: ProxyEventHandler): HandleResult {
        val event = EventFactory.createPluginEventHandlerRemoveEvent(handler)
        ProxyServer.getInstance().pluginManager.callEvent(event)
        if (event.isCancelled)
            return HandleResultImpl.create(Action.EVENT_HANDLER_REMOVE, false,
                I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Event-Handler-Remove.Event-Cancelled", getEventCancelledMessage(event)),
                Optional.of(handler.plugin))

        bcEventBaked[handler.event.clazz] = bcEventBaked[handler.event.clazz]!!.filterNot { it === handler.handle }.toTypedArray()

        for (v1 in bcListenerPriority.values) {
            for (v2 in v1.values) {
                for (listener in v2.keys) {
                    (v2 as MutableMap)[listener] = v2[listener]!!.filterNot { it === handler.method }.toTypedArray()
                }
            }
        }
        return HandleResultImpl.create(Action.EVENT_HANDLER_REMOVE, true,
            I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Event-Handler-Remove.Success-Removed-Event-Handler"),
            Optional.of(handler.plugin))
    }


    private fun searchPlugin(plugin: String) : File? {
        var result : File? = null

        val folder = ProxyServer.getInstance().pluginsFolder
        if (folder.exists()) {
            // First search for plugin description equals.
            result = listFiles(folder).firstOrNull {
                isDesNameEquals(it, plugin)
            // If not found, then search for plugin description contains.
            } ?: listFiles(folder).firstOrNull {
                isDesNameContains(it, plugin)
            // If not found, finally, search for file name contains.
            } ?: listFiles(folder).firstOrNull {
                isFileNameContains(it, plugin)
            // Or it's a module.
            } ?: listFiles(File(folder.parentFile, "modules")).firstOrNull {
                isDesNameEquals(it, plugin)
            }
        }

        return result
    }

    private fun isPluginJar(file: File) : Boolean {
        return file.isFile && file.name.endsWith(".jar", true)
    }

    private fun getPluginDesYaml(pluginJar: File) : PluginDescription? {
        val jar = JarFile(pluginJar)
        val pluginDesFile = jar.getJarEntry("bungee.yml") ?: jar.getJarEntry("plugin.yml")
        ?: return null

        val result = yaml.loadAs(jar.getInputStream(pluginDesFile), PluginDescription::class.java)
        if (result.file == null)
            result.file = pluginJar
        return result
    }

    private fun isDesNameEquals(file: File, name: String): Boolean {
        if (isPluginJar(file)) {
            return getPluginDesYaml(file)?.name?.contentEquals(name, true) == true
        }
        return false
    }

    private fun isDesNameContains(file: File, name: String): Boolean {
        if (isPluginJar(file) && name.length >= 4) {
            return getPluginDesYaml(file)?.name?.contains(name, true) == true
        }
        return false
    }

    private fun isFileNameContains(file: File, name: String): Boolean {
        if (isPluginJar(file)) {
            val plain = file.nameWithoutExtension
            if (plain.contains(name, true) && (name.length >= 4 || plain.length < 4)) {
                val jar = JarFile(file)
                return (jar.getJarEntry("bungee.yml") ?: jar.getJarEntry("plugin.yml")) != null
            }
        }
        return false
    }

    private fun disableFile(file: File) {
        val folder = file.parentFile
        val name = file.nameWithoutExtension
        var i = 0
        var renamed = File(folder, "$name.jar.disabled")
        if (!renamed.exists()) {
            processRename(file, renamed)
            return
        }
        while (i < 100) {
            renamed = File(folder, "$name (${++i}).jar.disabled")
            if (!renamed.exists()) {
                processRename(file, renamed)
                return
            }
        }
    }

    private fun enableFile(file: File): File {
        val folder = file.parentFile
        val name = file.nameWithoutExtension

        val renamed = if (name.endsWith(".jar", true))
            File(folder, name)
        else
            File(folder, "$name.jar")
        renamed.delete()
        return if (processRename(file, renamed)) renamed else file
    }

    private fun oldFile(file: File) {
        val folder = file.parentFile
        val name = file.nameWithoutExtension
        var i = 0
        var renamed = File(folder, "$name.jar.old")
        if (!renamed.exists()) {
            processRename(file, renamed)
            return
        }
        while (i < 100) {
            renamed = File(folder, "$name.jar.old${++i}")
            if (!renamed.exists()) {
                processRename(file, renamed)
                return
            }
        }
    }

    private fun processRename(from: File, to: File): Boolean {
        return try {
            Files.move(from.toPath(), to.toPath())
            true
        } catch (e: Throwable) {
            warn(I18nHelper.getLocaleMessage("Console-Sender.Rename-File.Failed-To-Rename", e.toString()))
            false
        }
    }

    private fun listFiles(folder: File): Array<out File> {
        return folder.listFiles()!!.sortedArrayWith(Comparator.comparingLong(File::lastModified).reversed())
    }

    private fun getEventCancelledMessage(e: AbstractBpmpEvent): String {
        return e.cancelledMessage ?: I18nHelper.getLocaleMessage("Sender.Event.Cancelled-Reasons.Default-Reason")
    }

}