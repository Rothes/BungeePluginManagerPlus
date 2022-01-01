package io.github.rothes.bungeepluginmanagerplus.bungeecord.internal

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.config.Configuration
import net.md_5.bungee.config.ConfigurationProvider
import net.md_5.bungee.config.YamlConfiguration
import java.io.File
import java.nio.file.Files
import java.util.*

object I18nHelper {

    private val osLocale by lazy {
        var locale: String = System.getProperty("user.language", Locale.getDefault().language)
        locale += '-'
        locale += System.getProperty("user.country", Locale.getDefault().country)
        locale
    }
    private lateinit var locale: String

    private var messages = mutableMapOf<String, String>()

    internal fun init() {
        exportDefaultConfig()
        locale = ConfigurationProvider.getProvider(YamlConfiguration::class.java)
            .load(File(plugin.dataFolder, "Config.yml")).getString("Options.Locale")
        exportLocales()
        loadLocale()
    }

    fun getPrefixedLocaleMessage(key: String, vararg replacements: String): String {
        var msg = messages[key]!!

        for (i in replacements.indices) {
            msg = msg.replace("%$i%", replacements[i])
        }
        return messages["Sender.Prefix"] + msg
    }

    fun getLocaleMessage(key: String, vararg replacements: String): String {
        var msg = messages[key]!!

        for (i in replacements.indices) {
            msg = msg.replace("%$i%", replacements[i])
        }
        return msg
    }

    private fun exportDefaultConfig() {
        val file = File(plugin.dataFolder, "Config.yml")
        if (file.exists())
            return

        (plugin.getResourceAsStream("Languages/$osLocale/Config.yml")
            ?: plugin.getResourceAsStream("Languages/en-US/Config.yml"))!!.use {
                Files.copy(it, file.toPath())
            }
    }

    private fun exportLocales() {
        val localeFile = File(plugin.dataFolder, "Locale/$locale/Message.yml")
        if (localeFile.exists())
            return

        localeFile.parentFile.mkdirs()
        (plugin.getResourceAsStream("Languages/$locale/Message.yml")
            ?: plugin.getResourceAsStream("Languages/$osLocale/Message.yml")
                ?: plugin.getResourceAsStream("Languages/en-US/Message.yml"))!!.use {
                    Files.copy(it, localeFile.toPath())
                }
    }

    private fun loadLocale() {
        val localeFile = File(plugin.dataFolder, "Locale/$locale/Message.yml")
        val config = ConfigurationProvider.getProvider(YamlConfiguration::class.java).load(localeFile)
        for (key in config.keys) {
            val get = config.get(key)
            if (get is String)
                messages[key] = ChatColor.translateAlternateColorCodes('&', get)
            else
                loadKeys(config, key)
        }
    }

    private fun loadKeys(config: Configuration, path: String) {
        val section = config.get(path)
        if (section is Configuration)
            for (key in section.keys) {
                val get = config.get("$path.$key")
                if (get is String)
                    messages["$path.$key"] = ChatColor.translateAlternateColorCodes('&', config.getString("$path.$key"))
                else
                    loadKeys(config, "$path.$key")
            }
    }

}