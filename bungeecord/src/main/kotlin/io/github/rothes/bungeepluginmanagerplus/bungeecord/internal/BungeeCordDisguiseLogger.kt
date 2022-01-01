package io.github.rothes.bungeepluginmanagerplus.bungeecord.internal

import java.util.logging.LogRecord
import java.util.logging.Logger

class BungeeCordDisguiseLogger internal constructor(
    private val prefix: String,
) : Logger("BungeeCord", null) {

    init {
        parent = plugin.proxy.logger
    }

    override fun log(record: LogRecord) {
        record.message = prefix + record.message
        super.log(record)
    }

}