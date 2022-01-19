package io.github.rothes.bungeepluginmanagerplus.bungeecord.api

import io.github.rothes.bungeepluginmanagerplus.api.Action
import io.github.rothes.bungeepluginmanagerplus.api.HandleResult
import io.github.rothes.bungeepluginmanagerplus.api.ProxyPlugin
import io.github.rothes.bungeepluginmanagerplus.bungeecord.internal.I18nHelper
import io.github.rothes.bungeepluginmanagerplus.bungeecord.internal.error
import io.github.rothes.bungeepluginmanagerplus.bungeecord.internal.info
import io.github.rothes.bungeepluginmanagerplus.bungeecord.internal.messageLocaled
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer

class HandleResultImpl private constructor(
    override val action: Action,
    override val success: Boolean,
    override val message: String,
    override val plugin: ProxyPlugin?,
) : HandleResult {

    internal fun sendResult(sender: CommandSender) {
        if (sender === ProxyServer.getInstance().console) {
            if (success) {
                info(I18nHelper.getLocaleMessage(action.getMainMessageNode(true)))
                info(message.replaceFirst(I18nHelper.getLocaleMessage("Sender.Prefix"), ""))
            } else {
                error(I18nHelper.getLocaleMessage(action.getMainMessageNode(false)))
                error(message.replaceFirst(I18nHelper.getLocaleMessage("Sender.Prefix"), ""))
            }
        } else {
            info(I18nHelper.getLocaleMessage(action.getMainMessageNode(success)))
            info(message.replaceFirst(I18nHelper.getLocaleMessage("Sender.Prefix"), ""))
            sender.messageLocaled(action.getMainMessageNode(success))
            @Suppress("DEPRECATION") sender.sendMessage(message)
        }
    }

    override fun toString(): String {
        return "HandleResultImpl{action=$action, success=$success, message=$message, plugin=$plugin}"
    }

    companion object Factory {

        fun create(action: Action, success: Boolean, message: String, plugin: ProxyPlugin?): HandleResult {
            return HandleResultImpl(action, success, message, plugin)
        }
    }

}