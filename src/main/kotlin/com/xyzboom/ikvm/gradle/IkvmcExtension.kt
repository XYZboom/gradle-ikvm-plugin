package com.xyzboom.ikvm.gradle

import org.gradle.api.Project

open class IkvmcExtension(private val project: Project) {
    lateinit var classLoader: String
    lateinit var assembly: String
}