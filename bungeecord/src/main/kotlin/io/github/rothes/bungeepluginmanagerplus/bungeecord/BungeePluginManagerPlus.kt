package io.github.rothes.bungeepluginmanagerplus.bungeecord

import io.github.rothes.bungeepluginmanagerplus.api.BungeePluginManagerPlusAPI
import io.github.rothes.bungeepluginmanagerplus.api.HandleResult
import io.github.rothes.bungeepluginmanagerplus.api.ProxyCommand
import io.github.rothes.bungeepluginmanagerplus.api.ProxyPlugin
import io.github.rothes.bungeepluginmanagerplus.bungeecord.internal.BungeeCordDisguiseLogger
import io.github.rothes.bungeepluginmanagerplus.bungeecord.internal.I18nHelper
import io.github.rothes.bungeepluginmanagerplus.bungeecord.internal.PluginManager
import io.github.rothes.bungeepluginmanagerplus.bungeecord.internal.Updater
import io.github.rothes.bungeepluginmanagerplus.bungeecord.internal.commands.CommandHandler
import io.github.rothes.bungeepluginmanagerplus.bungeecord.internal.info
import net.md_5.bungee.api.plugin.Plugin
import org.bstats.bungeecord.Metrics
import java.io.File


class BungeePluginManagerPlus : Plugin(), BungeePluginManagerPlusAPI {

    init {
        API = this
        hackPrefix()
    }

    private fun hackPrefix() {
        try {
            val logger = Plugin::class.java.getDeclaredField("logger")
            logger.isAccessible = true
            val prefix: String
            val background = ";48;2;5;15;40"
            val bracket = "\u001b[38;2;255;106;0" + background + "m"
            val gold = "\u001b[38;2;255;166;0" + background + "m"
            val deepGold = "\u001b[38;2;219;142;0" + background + "m"
            val blue = "\u001b[38;2;9;138;237" + background + "m"
            val reset = "\u001b[0m"
            prefix = "${bracket}[${deepGold}Bungee${gold}PluginManager${blue}+${bracket}]${reset} "
            logger[this] = BungeeCordDisguiseLogger(prefix)
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }

    override fun onEnable() {
        I18nHelper.init()
        proxy.pluginManager.registerCommand(this, CommandHandler)
        Updater.start()
        info(I18nHelper.getLocaleMessage("Console-Sender.Rename-File.Windows-Warning"))
        Metrics(this, 13875)
    }

    override fun getPlugins(): Array<ProxyPlugin> {
        return PluginManager.getPlugins()
    }

    override fun loadPlugin(plugin: String): HandleResult {
        return PluginManager.loadPlugin(plugin)
    }

    override fun loadPlugin(plugin: File): HandleResult {
        return PluginManager.loadPlugin(plugin)
    }

    override fun unloadPlugin(plugin: String): HandleResult {
        return PluginManager.unloadPlugin(plugin)
    }

    override fun reloadPlugin(plugin: String): HandleResult {
        return PluginManager.reloadPlugin(plugin)
    }

    override fun enablePlugin(plugin: String): HandleResult {
        return PluginManager.enablePlugin(plugin)
    }

    override fun disablePlugin(plugin: String): HandleResult {
        return PluginManager.disablePlugin(plugin)
    }

    override fun updatePlugin(plugin: String): HandleResult {
        return PluginManager.updatePlugin(plugin)
    }

    override fun getCommandByName(command: String): ProxyCommand? {
        return PluginManager.getCommandByName(command)
    }

    override fun getCommandsByPlugin(plugin: ProxyPlugin): Array<ProxyCommand> {
        return PluginManager.getCommandsByPlugin(plugin)
    }

    override fun getCommandsAll(): Array<ProxyCommand> {
        return PluginManager.getCommandsAll()
    }

    override fun removeCommand(command: ProxyCommand): HandleResult {
        return PluginManager.removeCommand(command)
    }

    companion object {
        @JvmStatic
        lateinit var API: BungeePluginManagerPlusAPI
            private set
    }

}