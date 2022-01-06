package io.github.rothes.bungeepluginmanagerplus.api

import java.io.File

interface BungeePluginManagerPlusAPI {

    fun getPlugins(): Array<ProxyPlugin>
    fun loadPlugin(plugin: String): HandleResult
    fun loadPlugin(plugin: File): HandleResult
    fun unloadPlugin(plugin: String): HandleResult
    fun reloadPlugin(plugin: String): HandleResult
    fun enablePlugin(plugin: String): HandleResult
    fun disablePlugin(plugin: String): HandleResult
    fun updatePlugin(plugin: String): HandleResult
    fun getCommandByName(command: String): ProxyCommand?
    fun getCommandsByPlugin(plugin: ProxyPlugin): Array<ProxyCommand>
    fun getCommandsAll(): Array<ProxyCommand>
    fun removeCommand(command: ProxyCommand): HandleResult

}