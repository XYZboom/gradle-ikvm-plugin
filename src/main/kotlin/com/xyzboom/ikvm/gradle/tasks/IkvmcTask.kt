package com.xyzboom.ikvm.gradle.tasks

import com.xyzboom.ikvm.gradle.IkvmcExtension
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.getByName
import java.lang.StringBuilder

open class IkvmcTask : IkvmTask() {

    @get:Internal
    internal lateinit var ikvmcExtension: IkvmcExtension

    @TaskAction
    fun doIkvmCompile() {
        initIkvmEnv()
        val jarFile = project.tasks.getByName<Jar>("jar").archiveFile.get().asFile
        val args = arrayListOf<String>(ikvmcExe.toString(), jarFile.absolutePath)
        try {
            val classLoader = ikvmcExtension.classLoader
            args += "-classloader:$classLoader"
        } catch (e: Exception) {
            when (e) {
                is UninitializedPropertyAccessException, is NullPointerException -> {
                    // do nothing
                }

                else -> throw e
            }
        }
        val assembly = try {
            ikvmcExtension.assembly
        } catch (e: Exception) {
            when (e) {
                is UninitializedPropertyAccessException, is NullPointerException -> {
                    project.name
                }

                else -> throw e
            }
        }
        args += "-assembly:$assembly"
        args += "-out:${jarFile.parentFile.absolutePath}/$assembly.dll"
        project.exec {
            commandLine(args)
        }
    }
}