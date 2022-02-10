package io.github.rothes.bungeepluginmanagerplus.bungeecord.internal

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.config.Configuration
import net.md_5.bungee.config.ConfigurationProvider
import net.md_5.bungee.config.YamlConfiguration
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.util.*

object I18nHelper {

    private val osLocale by lazy {
        var locale: String = System.getProperty("user.language", Locale.getDefault().language)
        locale += '-'
        locale += System.getProperty("user.country", Locale.getDefault().country)
        locale
    }
    internal lateinit var locale: String
        private set
    private lateinit var localeConfig: Configuration

    private var messages = mutableMapOf<String, String>()

    internal fun init() {
        val configFile = File(plugin.dataFolder, "Config.yml")
        if (!configFile.exists()) {
            configFile.parentFile.mkdirs()
            getDefaultConfig().use {
                Files.copy(it, configFile.toPath())
            }
        }
        locale = ConfigurationProvider.getProvider(YamlConfiguration::class.java)
            .load(File(plugin.dataFolder, "Config.yml")).getString("Options.Locale")

        val localeFile = File(plugin.dataFolder, "Locales/$locale/Message.yml")
        if (!localeFile.exists()) {
            localeFile.parentFile.mkdirs()
            getDefaultLocale().use {
                Files.copy(it, localeFile.toPath())
            }
        }
        localeConfig = ConfigurationProvider.getProvider(YamlConfiguration::class.java).load(localeFile)
        messages.putAll(getLocaleKeys(localeConfig))
        checkLocaleKeys()
    }

    fun getPrefixedLocaleMessage(key: String, vararg replacements: String): String {
        return getPrefixedLocaleMessage(key, replacements)
    }

    @JvmName("getPrefixedLocaleMessage1")
    fun getPrefixedLocaleMessage(key: String, replacements: Array<out String>): String {
        return getLocaleMessage("Sender.Prefix") + getLocaleMessage(key, replacements)
    }

    fun getLocaleMessage(key: String, vararg replacements: String): String {
        return getLocaleMessage(key, replacements)
    }

    @JvmName("getLocaleMessage1")
    fun getLocaleMessage(key: String, replacements: Array<out String>): String {
        var msg = messages[key] ?: "§cMissing locale key: $key"

        for (i in replacements.indices) {
            msg = msg.replace("%$i%", replacements[i])
        }
        return msg
    }

    private fun getDefaultConfig(): InputStream {
        return (plugin.getResourceAsStream("Languages/$osLocale/Config.yml")
            ?: plugin.getResourceAsStream("Languages/en-US/Config.yml"))!!
    }

    private fun getDefaultLocale(): InputStream {
        return (plugin.getResourceAsStream("Languages/$locale/Message.yml")
            ?: plugin.getResourceAsStream("Languages/$osLocale/Message.yml")
                ?: plugin.getResourceAsStream("Languages/en-US/Message.yml"))!!
    }

    private fun checkLocaleKeys() {
        val default = getDefaultLocale().use { stream ->
            stream.reader(Charsets.UTF_8).use {
                ConfigurationProvider.getProvider(YamlConfiguration::class.java).load(it)
            }
        }
        for (entry in getLocaleKeys(default).entries) {
            if (!messages.containsKey(entry.key)) {
                messages[entry.key] = entry.value
                localeConfig.set(entry.key, entry.value)
            }
        }
        ConfigurationProvider.getProvider(YamlConfiguration::class.java)
            .save(localeConfig, File(plugin.dataFolder, "Locales/$locale/Message.yml"))
    }

    private fun getLocaleKeys(config: Configuration): Map<String, String> {
        val map = mutableMapOf<String, String>()
        for (key in config.keys) {
            val get = config.get(key)
            if (get !is Configuration && get !is List<*>)
                map[key] = ChatColor.translateAlternateColorCodes('&', get.toString())
            else if (get is Configuration)
                addLocaleKeys(map, config, key)
        }
        return map
    }

    private fun addLocaleKeys(map: MutableMap<String, String>, config: Configuration, path: String) {
        val section = config.get(path) as Configuration
        for (key in section.keys) {
            val fullKey = "$path.$key"
            val get = config.get(fullKey)
            if (get !is Configuration && get !is List<*>)
                map[fullKey] = ChatColor.translateAlternateColorCodes('&', config.getString(fullKey))
            else addLocaleKeys(map, config, fullKey)
        }
    }

}