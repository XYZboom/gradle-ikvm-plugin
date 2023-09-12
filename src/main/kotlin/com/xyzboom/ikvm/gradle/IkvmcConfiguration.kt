package com.xyzboom.ikvm.gradle

import org.gradle.api.Project
import kotlin.properties.Delegates

open class IkvmcConfiguration(private val project: Project) {
    open inner class IkvmcDependenciesConfig {
        internal var handleDependencies = false
        var mergeDependencies = true
        var mergeName = "${project.name}-ikvm-dependencies"
    }

    lateinit var classLoader: String
    lateinit var assembly: String
    var signWithKeyFile = true
    var debug by Delegates.notNull<Boolean>()
    internal var dependenciesConfig: IkvmcDependenciesConfig = IkvmcDependenciesConfig()

    val extraCmdArgs = ArrayList<String>()

    @Suppress("Unused")
    fun ikvmcDependencies(ikvmcDependenciesHandler: IkvmcDependenciesConfig.() -> Unit) {
        dependenciesConfig.handleDependencies = true
        dependenciesConfig.ikvmcDependenciesHandler()
    }
}