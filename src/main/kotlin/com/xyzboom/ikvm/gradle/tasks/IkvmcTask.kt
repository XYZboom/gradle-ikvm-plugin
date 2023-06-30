package com.xyzboom.ikvm.gradle.tasks

import org.gradle.api.tasks.TaskAction

open class IkvmcTask : IkvmTask() {
    @TaskAction
    fun doIkvmCompile() {
        println("hello ikvmc")
    }
}