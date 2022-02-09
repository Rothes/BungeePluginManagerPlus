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
import java.util.*

class HandleResultImpl private constructor(
    override val action: Action,
    override val success: Boolean,
    override val message: String,
    override val plugin: Optional<ProxyPlugin>,
) : HandleResult {

    internal fun sendResult(sender: CommandSender) {
        val message1 = I18nHelper.getLocaleMessage(action.getMainMessageNode(success))
        val message2 = message.replaceFirst(I18nHelper.getLocaleMessage("Sender.Prefix"), "")
        if (success) {
            info(message1)
            info(message2)
        } else {
            error(message1)
            error(message2)
        }
        if (sender !== ProxyServer.getInstance().console) {
            sender.messageLocaled(action.getMainMessageNode(success))
            @Suppress("DEPRECATION") sender.sendMessage(message)
        }
    }

    override fun equals(other: Any?): Boolean {
        return other is HandleResultImpl && other.action == this.action && other.success == this.success
                && other.plugin === this.plugin && other.message == this.message
    }

    override fun hashCode(): Int {
        return Objects.hash(action, success, message, plugin)
    }

    override fun toString(): String {
        return "HandleResultImpl{action=$action, success=$success, message=$message, plugin=$plugin}"
    }

    companion object Factory {

        fun create(action: Action, success: Boolean, message: String, plugin: Optional<ProxyPlugin>): HandleResult {
            return HandleResultImpl(action, success, message, plugin)
        }

        fun create(action: Action, success: Boolean, message: String, plugin: ProxyPlugin?): HandleResult {
            return HandleResultImpl(action, success, message, Optional.ofNullable(plugin))
        }

    }

}