package io.github.rothes.bungeepluginmanagerplus.api

enum class Action {

    PLUGIN_LOAD {
        override fun getMainMessageNode(success: Boolean): String {
            return if (success) "Sender.Commands.Load.Success-Main-Message" else "Sender.Commands.Load.Failed-Main-Message"
        }
    },
    PLUGIN_UNLOAD {
        override fun getMainMessageNode(success: Boolean): String {
            return if (success) "Sender.Commands.Unload.Success-Main-Message" else "Sender.Commands.Unload.Failed-Main-Message"
        }
    },
    PLUGIN_RELOAD {
        override fun getMainMessageNode(success: Boolean): String {
            return if (success) "Sender.Commands.Reload.Success-Main-Message" else "Sender.Commands.Reload.Failed-Main-Message"
        }
    },
    PLUGIN_ENABLE {
        override fun getMainMessageNode(success: Boolean): String {
            return if (success) "Sender.Commands.Enable.Success-Main-Message" else "Sender.Commands.Enable.Failed-Main-Message"
        }
    },
    PLUGIN_DISABLE {
        override fun getMainMessageNode(success: Boolean): String {
            return if (success) "Sender.Commands.Disable.Success-Main-Message" else "Sender.Commands.Disable.Failed-Main-Message"
        }
    },
    PLUGIN_UPDATE {
        override fun getMainMessageNode(success: Boolean): String {
            return if (success) "Sender.Commands.Update.Success-Main-Message" else "Sender.Commands.Update.Failed-Main-Message"
        }
    },
    COMMAND_REMOVE {
        override fun getMainMessageNode(success: Boolean): String {
            return if (success) "Sender.Commands.Command-Remove.Success-Main-Message" else "Sender.Commands.Command-Remove.Failed-Main-Message"
        }
    },
    EVENT_LISTENER_REMOVE {
        override fun getMainMessageNode(success: Boolean): String {
            return if (success) "Sender.Commands.Event-Listener-Remove.Success-Main-Message" else "Sender.Commands.Event-Listener-Remove.Failed-Main-Message"
        }
    },
    EVENT_HANDLER_REMOVE {
        override fun getMainMessageNode(success: Boolean): String {
            return if (success) "Sender.Commands.Event-Handler-Remove.Success-Main-Message" else "Sender.Commands.Event-Handler-Remove.Failed-Main-Message"
        }
    };

    abstract fun getMainMessageNode(success: Boolean) : String

}