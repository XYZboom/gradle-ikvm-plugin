package com.xyzboom.ikvm.gradle.tasks

import com.xyzboom.ikvm.gradle.IkvmcConfiguration
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.getByName
import java.io.File

open class IkvmcTask : IkvmTask() {

    @get:Internal
    internal lateinit var ikvmcConfiguration: IkvmcConfiguration

    @get:Internal
    internal lateinit var jarFile: File

    @get:Internal
    internal lateinit var args: MutableList<String>

    companion object {
        const val ArgAssemblyPrefix = "-assembly:"
        const val ArgOutputPrefix = "-out:"
        const val ArgReferencePrefix = "-r:"
    }

    private fun handleClassLoader() {
        try {
            val classLoader = ikvmcConfiguration.classLoader
            args.add("-classloader:$classLoader")
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
                project.exec {
                    commandLine(
                        ikvmcExe.absolutePath, *jarFiles.toTypedArray(),
                        "$ArgAssemblyPrefix$mergeName",
                        "$ArgOutputPrefix$dependDllPath"
                    )
                }
            }
            args.addAll(dependDlls.map { "$ArgReferencePrefix$it" })
        }
    }

    @TaskAction
    fun doIkvmCompile() {
        initIkvmEnv()
        jarFile = project.tasks.getByName<Jar>("jar").archiveFile.get().asFile
        args = arrayListOf(ikvmcExe.toString(), jarFile.absolutePath)
        handleClassLoader()
        handleAssembly()
        handleDependencies()
        args.addAll(ikvmcConfiguration.extraCmdArgs)
        val myArgs = args
        project.exec {
            commandLine(myArgs)
        }
    }
}