package com.xyzboom.ikvm.gradle

import com.xyzboom.ikvm.gradle.tasks.IkvmTask
import com.xyzboom.ikvm.gradle.tasks.IkvmcTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskExecutionException

@Suppress("Unused")
class IkvmPlugin : Plugin<Project> {
    companion object {
        private const val IKVM_HOME_NAME = "ikvmHome"
    }

    override fun apply(target: Project) {
        target.extensions.create("ikvm", IkvmExtension::class.java)
        target.extensions.create("ikvmc", IkvmcExtension::class.java)
        target.tasks.register("ikvmc", IkvmcTask::class.java) {
            group = "ikvm"
            dependsOn("jar")
        }
        target.tasks.withType(IkvmTask::class.java) {
            val ikvmExtension = target.extensions.getByType(IkvmExtension::class.java)
            try {
                ikvmHome = ikvmExtension.ikvmHome
            } catch (e: Exception) {
                when (e) {
                    is UninitializedPropertyAccessException, is NullPointerException -> {
                        val ikvmHome = System.getenv()["IKVM_HOME"]
                            ?: throw TaskExecutionException(
                                this,
                                Exception("No system environment IKVM_HOME found! Please set one.")
                            )
                        ikvmExtension.ikvmHome = ikvmHome
                        this.ikvmHome = ikvmExtension.ikvmHome
                    }

                    else -> throw e
                }
            }
        }
        target.tasks.withType(IkvmcTask::class.java) {
            val ikvmExtension = target.extensions.getByType(IkvmExtension::class.java)
            val ikvmcExtension: IkvmcExtension = try {
                @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
                ikvmExtension.ikvmcExtension!!
            } catch (e: Exception) {
                when (e) {
                    is UninitializedPropertyAccessException, is NullPointerException -> {
                        ikvmExtension.ikvmcExtension =
                            target.extensions.getByType(IkvmcExtension::class.java)
                        ikvmExtension.ikvmcExtension
                    }

                    else -> throw e
                }
            }
            this.ikvmcExtension = ikvmcExtension
        }
    }
}