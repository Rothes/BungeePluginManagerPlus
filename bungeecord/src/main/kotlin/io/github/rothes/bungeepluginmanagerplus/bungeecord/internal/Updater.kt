package io.github.rothes.bungeepluginmanagerplus.bungeecord.internal

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.md_5.bungee.api.ProxyServer
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit


object Updater {

    private const val VERSION_CHANNCEL = "Stable"
    private const val VERSION_NUMBER = 3
    private val msgTimesMap = mutableMapOf<String, Int>()
    private val HOST_STRING: String by lazy {
        if (I18nHelper.locale == "zh-CN") "raw.fastgit.org"
        else "raw.githubusercontent.com"
    }

    internal fun start() {
        ProxyServer.getInstance().scheduler.schedule(plugin, {
            try {
                val jsonString = getJson()
                checkJson(jsonString)
            } catch (e: IOException) {
                warn(I18nHelper.getLocaleMessage("Console-Sender.Updater.Error-Checking-Version-Connection", e.toString()))
            } catch (e: IllegalStateException) {
                warn(I18nHelper.getLocaleMessage("Console-Sender.Updater.Error-Parsing-Json", e.toString()))
            } catch (e: NullPointerException) {
                warn(I18nHelper.getLocaleMessage("Console-Sender.Updater.Error-Parsing-Json", e.toString()))
            }

        }, 0L, 1L, TimeUnit.HOURS)
    }

    private fun getJson(): String {
        val url = URL("https://$HOST_STRING/Rothes/BungeePluginManagerPlus/master/Version%20Infos.json")
        val jsonBuilder: StringBuilder = StringBuilder()
        url.openStream().use {
            BufferedReader(InputStreamReader(it, StandardCharsets.UTF_8)).use { reader ->
                var line = reader.readLine()
                while (line != null) {
                    jsonBuilder.append(line).append("\n")
                    line = reader.readLine()
                }
            }
        }
        return jsonBuilder.toString()
    }

    private fun checkJson(json: String) {
        val element = JsonParser.parseString(json)
        val root = element.asJsonObject
        val channels = root.getAsJsonObject("Version_Channels")
        if (channels.has(VERSION_CHANNCEL)) {
            val channel = channels.getAsJsonObject(VERSION_CHANNCEL)
            if (channel.has("Message")
                && channel.getAsJsonPrimitive("Latest_Version_Number").asString.toInt() > VERSION_NUMBER) {
                sendLocaledJsonMessage(channel, "updater")
            }
        } else {
            warn(I18nHelper.getLocaleMessage("Console-Sender.Updater.Invalid-Channel", VERSION_CHANNCEL))
        }

        for (entry in root.getAsJsonObject("Version_Actions").entrySet()) {
            val split = entry.key.split("-")
            if (Integer.parseInt(split[1]) > VERSION_NUMBER
                && VERSION_NUMBER > Integer.parseInt(split[0])) {
                val message = entry.value as JsonObject
                if (message.has("Message"))
                    sendLocaledJsonMessage(message, entry.key)
            }
        }

    }

    private fun sendLocaledJsonMessage(json: JsonObject, id: String) {
        val msgJson = json.getAsJsonObject("Message")
        val msg: String = if (msgJson.has(I18nHelper.locale)) {
            msgJson[I18nHelper.locale].asString
        } else {
            msgJson["en-US"].asString
        }

        val msgTimes = json.get("Message_Times")?.asInt ?: -1
        val curTimes = if (msgTimesMap[id] == null) 0 else msgTimesMap[id]!!

        if (msgTimes == -1 || curTimes < msgTimes) {

            val logLevel = json.get("Log_Level")?.asString ?: "default maybe"

            for (s in msg.split("\n")) {
                when (logLevel) {
                    "Error" -> error(s)
                    "Warn"  -> warn(s)
                    "Info"  -> info(s)
                    else    -> info(s)
                }
            }
            msgTimesMap[id] = curTimes + 1
        }

        for (action in json.getAsJsonArray("Actions")) {
            if (action.asString == "Prohibit") {
                PluginManager.unloadPlugin("BungeePluginManagerPlus")
            }
        }
    }

}