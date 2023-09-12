package com.xyzboom.ikvm.gradle.tasks

import com.xyzboom.ikvm.gradle.IkvmcConfiguration
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.getByName
import java.io.File

open class IkvmcTask : IkvmTask() {

    @get:Internal
    internal lateinit var ikvmcConfiguration: IkvmcConfiguration

    @get:Internal
    internal lateinit var jarFile: File

    @get:Internal
    internal lateinit var args: MutableList<String>

    @get:Internal
    internal lateinit var mergeArgs: MutableList<String>

    companion object {
        private const val PropertyKeyPrefix = "ikvmc."
        const val PropertyDebugKey = "${PropertyKeyPrefix}debug"
        const val ArgAssemblyPrefix = "-assembly:"
        const val ArgClassLoaderPrefix = "-classloader:"
        const val ArgOutputPrefix = "-out:"
        const val ArgReferencePrefix = "-r:"
        const val ArgKeyFilePrefix = "-keyfile:"
        const val ArgDebug = "-debug"
    }

    private fun handleClassLoader() {
        try {
            val classLoader = ikvmcConfiguration.classLoader
            args.add("$ArgClassLoaderPrefix$classLoader")
            if (::mergeArgs.isInitialized) {
                mergeArgs.add("$ArgClassLoaderPrefix$classLoader")
            }
        } catch (e: Exception) {
            when (e) {
                is UninitializedPropertyAccessException, is NullPointerException -> {
                    // do nothing
                }

                else -> throw e
            }
        }
    }

    private fun handleAssembly() {
        val assembly = try {
            ikvmcConfiguration.assembly
        } catch (e: Exception) {
            when (e) {
                is UninitializedPropertyAccessException, is NullPointerException -> {
                    project.name
                }

                else -> throw e
            }
        }
        args.add("$ArgAssemblyPrefix$assembly")
        println("ikvmc output file: ${jarFile.parentFile.absolutePath}/$assembly.dll")
        args.add("-out:${jarFile.parentFile.absolutePath}/$assembly.dll")
    }

    private fun handleDependencies() {
        if (ikvmcConfiguration.dependenciesConfig.handleDependencies) {
            val configuration = project.configurations.getByName("runtimeClasspath")
            val artifacts = configuration.resolvedConfiguration.resolvedArtifacts
            val dependDlls = ArrayList<String>()
            if (ikvmcConfiguration.dependenciesConfig.mergeDependencies) {
                val jarFiles = artifacts.map { it.file.absolutePath }
                val mergeName = ikvmcConfiguration.dependenciesConfig.mergeName
                val dependDllPath = "${jarFile.parentFile.absolutePath}/$mergeName.dll"
                dependDlls.add(dependDllPath)
                mergeArgs = arrayListOf(
                    ikvmcExe.absolutePath, *jarFiles.toTypedArray(),
                    "$ArgAssemblyPrefix$mergeName",
                    "$ArgOutputPrefix$dependDllPath"
                )
            }
            args.addAll(dependDlls.map { "$ArgReferencePrefix$it" })
        }
    }

    private fun handleKeyFile() {
        if (!ikvmcConfiguration.signWithKeyFile) return
        val keyFile = System.getenv("IKVM.KEYFILE") ?: kotlin.run {
            println("no key file found in system env IKVM.KEYFILE")
        }
        println("key file in system env is $keyFile")
        args.add("$ArgKeyFilePrefix$keyFile")
        if (::mergeArgs.isInitialized) {
            mergeArgs.add("$ArgKeyFilePrefix$keyFile")
        }
    }

    private fun handleDebug() {
        val debug = try {
            ikvmcConfiguration.debug
        } catch (e: Exception) {
            project.properties[PropertyDebugKey].toString().toBoolean()
        }
        if (debug) {
            println("ikvmc debug enabled")
            args.add(ArgDebug)
            if (::mergeArgs.isInitialized) {
                mergeArgs.add(ArgDebug)
            }
        } else {
            println("ikvmc debug disabled")
        }
    }

    @TaskAction
    fun doIkvmCompile() {
        initIkvmEnv()
        jarFile = project.tasks.getByName<Jar>("jar").archiveFile.get().asFile
        args = arrayListOf(ikvmcExe.toString(), jarFile.absolutePath)
        handleAssembly()
        handleDependencies()
        handleClassLoader()
        handleKeyFile()
        handleDebug()
        args.addAll(ikvmcConfiguration.extraCmdArgs)
        val myArgs = args
        if (this@IkvmcTask::mergeArgs.isInitialized) {
            project.exec {
                println("exec merge: ${myArgs.joinToString(" ")}")
                commandLine(mergeArgs)
            }
        }
        project.exec {
            println("exec: ${myArgs.joinToString(" ")}")
            commandLine(myArgs)
        }
    }
}