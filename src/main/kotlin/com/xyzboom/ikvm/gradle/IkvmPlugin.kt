package com.xyzboom.ikvm.gradle

import com.xyzboom.ikvm.gradle.tasks.IkvmcTask
import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("Unused")
class IkvmPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.tasks.register("ikvmc", IkvmcTask::class.java) {
            it.group = "ikvm"
            it.dependsOn("jar")
        }
    }
}