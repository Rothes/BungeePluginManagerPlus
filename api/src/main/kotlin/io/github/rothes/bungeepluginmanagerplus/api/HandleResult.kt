package io.github.rothes.bungeepluginmanagerplus.api

interface HandleResult {

    val action: Action
    val success: Boolean
    val message: String
    val plugin: ProxyPlugin?

}