package io.github.rothes.bungeepluginmanagerplus.api

enum class Action {

    LOAD {
        override fun getMainMessageNode(success: Boolean): String {
            return if (success) "Sender.Commands.Load.Success-Main-Message" else "Sender.Commands.Load.Failed-Main-Message"
        }
    },
    UNLOAD {
        override fun getMainMessageNode(success: Boolean): String {
            return if (success) "Sender.Commands.Unload.Success-Main-Message" else "Sender.Commands.Unload.Failed-Main-Message"
        }
    },
    RELOAD {
        override fun getMainMessageNode(success: Boolean): String {
            return if (success) "Sender.Commands.Reload.Success-Main-Message" else "Sender.Commands.Reload.Failed-Main-Message"
        }
    };

    abstract fun getMainMessageNode(success: Boolean) : String

}