package io.github.rothes.bungeepluginmanagerplus.bungeecord.internal

import io.github.rothes.bungeepluginmanagerplus.api.Action
import io.github.rothes.bungeepluginmanagerplus.api.HandleResult
import io.github.rothes.bungeepluginmanagerplus.api.ProxyPlugin
import io.github.rothes.bungeepluginmanagerplus.bungeecord.api.HandleResultImpl
import io.github.rothes.bungeepluginmanagerplus.bungeecord.api.ProxyPluginImpl
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.api.plugin.PluginDescription
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.lang.reflect.Method
import java.util.Stack
import java.util.jar.JarFile
import kotlin.collections.HashMap
import net.md_5.bungee.api.plugin.PluginManager as BungeePluginManager

object PluginManager {

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
    private val enablePluginMethod: Method by lazy {
        val method = BungeePluginManager::class.java.getDeclaredMethod("enablePlugin",
            Map::class.java, Stack::class.java, PluginDescription::class.java)
        method.isAccessible = true
        method
    }


    internal fun loadPlugin(plugin: String): HandleResult {
        val file = searchPlugin(plugin) ?: return HandleResultImpl(Action.LOAD, false,
            I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Load.Plugin-Not-Found"))

        return loadPlugin(file)
    }

    internal fun loadPlugin(plugin: File): HandleResult {
        val pluginDes = getPluginDesYaml(plugin) ?: return HandleResultImpl(Action.LOAD, false,
            I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Load.Invalid-Plugin-Description"))
        for (depend in pluginDes.depends) {
            ProxyServer.getInstance().pluginManager.getPlugin(depend)
                ?: return HandleResultImpl(Action.LOAD, false,
                    I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Load.Missing-Dependency", depend))
        }
        if (bcPlugins.containsKey(pluginDes.name)) return HandleResultImpl(Action.LOAD, false,
            I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Load.Plugin-Already-Loaded"))

        return try {
            val success = enablePluginMethod.invoke(ProxyServer.getInstance().pluginManager,
                HashMap<PluginDescription, Boolean>(), Stack<PluginDescription>(), pluginDes) as Boolean

            with (bcPlugins[pluginDes.name]) {
                this?.onLoad()
                this?.onEnable()
            }
            pluginDes.file = plugin
            bcToLoad[pluginDes.name] = pluginDes
            HandleResultImpl(Action.LOAD, success,
                if (success) I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Load.Success-Loaded-Plugin", pluginDes.name)
                else I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Load.Failed-Loading-Plugin", pluginDes.name))
        } catch (e: Throwable) {
            HandleResultImpl(Action.LOAD, false,
                I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Load.Failed-Loading-Plugin", pluginDes.name))
        }
    }

    internal fun unloadPlugin(plugin: String): HandleResult {
        val instance = bcPlugins[plugin] ?: bcPlugins[bcPlugins.keys.firstOrNull { it.equals(plugin, true) }
        ] ?: return HandleResultImpl(Action.UNLOAD, false,
            I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Unload.Plugin-Not-Exist", plugin))

        return try {
            ProxyServer.getInstance().pluginManager.unregisterCommands(instance)
            ProxyServer.getInstance().pluginManager.unregisterListeners(instance)
            ProxyServer.getInstance().scheduler.cancel(instance)
            instance.onDisable()
            bcPlugins.remove(instance.description.name)
            bcToLoad.remove(instance.description.name)
            HandleResultImpl(Action.UNLOAD, true,
                I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Unload.Success-Unloaded-Plugin", instance.description.name))
        } catch (e: Throwable) {
            HandleResultImpl(Action.UNLOAD, false,
                I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Unload.Failed-Unloading-Plugin", instance.description.name))
        }
    }

    internal fun reloadPlugin(plugin: String): HandleResult {
        val unload = unloadPlugin(plugin)
        if (!unload.success)
            return HandleResultImpl(Action.RELOAD, false, unload.message)
        val load = loadPlugin(plugin)
        if (!load.success)
            return HandleResultImpl(Action.RELOAD, false, load.message)
        return HandleResultImpl(Action.RELOAD, true,
            I18nHelper.getPrefixedLocaleMessage("Sender.Commands.Reload.Success-Reloaded-Plugin", plugin))
    }

    internal fun getPlugins() : Array<ProxyPlugin> {
        val plugins = ProxyServer.getInstance().pluginManager.plugins
        val list = mutableListOf<ProxyPlugin>()
        for (plugin in plugins) {
            list.add(ProxyPluginImpl(plugin.description.name, plugin))
        }
        return list.also { it ->
            it.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
        }.toTypedArray()
    }

    private fun searchPlugin(plugin: String) : File? {
        var result : File? = null

        val folder = ProxyServer.getInstance().pluginsFolder
        if (folder.exists()) {
            // First search for plugin description equals.
            result = folder.listFiles()?.firstOrNull {
                isDesNameEquals(it, plugin)
            // If not found, then search for plugin description contains.
            } ?: folder.listFiles()?.firstOrNull {
                isDesNameContains(it, plugin)
            // If not found, finally, search for file name contains.
            } ?: folder.listFiles()?.firstOrNull {
                isFileNameContains(it, plugin)
            // Or it's a module.
            } ?: File(folder.parentFile, "modules").listFiles()?.firstOrNull() {
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

        return Yaml().loadAs(jar.getInputStream(pluginDesFile), PluginDescription::class.java)
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
        if (isPluginJar(file) && (file.nameWithoutExtension.contentEquals(name, true) ||
                    (name.length >= 4 && file.nameWithoutExtension.contains(name, true)))) {
            val jar = JarFile(file)
            return (jar.getJarEntry("bungee.yml") ?: jar.getJarEntry("plugin.yml")) != null
        }
        return false
    }

}