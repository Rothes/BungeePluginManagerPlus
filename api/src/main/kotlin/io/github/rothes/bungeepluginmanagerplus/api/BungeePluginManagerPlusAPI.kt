package io.github.rothes.bungeepluginmanagerplus.api

import java.io.File

interface BungeePluginManagerPlusAPI {

    fun loadPlugin(plugin: String): HandleResult
    fun loadPlugin(plugin: File): HandleResult
    fun unloadPlugin(plugin: String): HandleResult
    fun reloadPlugin(plugin: String): HandleResult

}