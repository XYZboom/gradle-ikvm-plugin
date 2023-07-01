package com.xyzboom.ikvm.gradle

import org.gradle.api.Project

open class IkvmExtension(
    private val project: Project,
) {
    lateinit var ikvmHome: String
    internal lateinit var ikvmcConfiguration: IkvmcConfiguration

    @Suppress("Unused")
    fun ikvmc(ikvmcHandler: IkvmcConfiguration.() -> Unit) {
        println("set ikvmcConfiguration")
        ikvmcConfiguration = IkvmcConfiguration(project)
        ikvmcHandler.invoke(ikvmcConfiguration)
    }
}