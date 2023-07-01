package com.xyzboom.ikvm.gradle.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskExecutionException
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*

abstract class IkvmTask : DefaultTask() {
    @get:Internal
    lateinit var ikvmHome: String

    @get:Internal
    internal lateinit var ikvmcExe: File

    @get:Internal
    internal lateinit var ikvmVersion: String

    private fun initIkvmVersion() {
        val byteStream = ByteArrayOutputStream()
        project.exec {
            val execSpec = commandLine("$ikvmcExe ")
            execSpec.errorOutput = byteStream
        }
        val scanner = Scanner(byteStream.toString())
        val versionLine = scanner.nextLine()
        val vReg = Regex("(IKVM\\.NET Compiler version )(\\d+\\.\\d+\\.\\d+\\.\\d+)")
        val matchResult = vReg.find(versionLine)
            ?: throw TaskExecutionException(
                this,
                Exception("Could not read ikvm version from: \n$byteStream")
            )
        if (matchResult.groupValues.size < 2) {
            throw TaskExecutionException(
                this,
                Exception("Could not read ikvm version from: \n$byteStream")
            )
        }
        // as groupValues access index 0 with integer 1, so
        // we want the 2nd value, use 1 + 1
        ikvmVersion = matchResult.groupValues[1 + 1]
    }

    protected fun initIkvmEnv() {
        val ikvmHomeFile = if (File(ikvmHome).isAbsolute) {
            File(ikvmHome)
        } else {
            File(project.projectDir, ikvmHome)
        }

        println("your ikvm home is $ikvmHomeFile")
        ikvmcExe = File(ikvmHomeFile, "bin/ikvmc.exe").absoluteFile
        if (!ikvmcExe.exists()) {
            throw TaskExecutionException(this, Exception("No ikvmc.exe found in $ikvmcExe"))
        }
        initIkvmVersion()
        println("ikvm version: $ikvmVersion")
    }
}