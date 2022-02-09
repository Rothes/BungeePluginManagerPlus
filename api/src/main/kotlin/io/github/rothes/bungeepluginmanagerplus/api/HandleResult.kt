package io.github.rothes.bungeepluginmanagerplus.api

import java.util.*

interface HandleResult {

    val action: Action
    val success: Boolean
    val message: String
    val plugin: Optional<ProxyPlugin>

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

}