package io.github.ackeescreenshotter.android

import java.io.File
import java.util.Properties
import org.gradle.api.Plugin
import org.gradle.api.Project

val libProperties = Properties()

// TODO No idea why this can't be loaded. Discuss with Šimi when he comes back.
class BuldLogicPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        libProperties.also { it.load(File("${target.rootDir}/lib.properties").reader()) }
    }
}



