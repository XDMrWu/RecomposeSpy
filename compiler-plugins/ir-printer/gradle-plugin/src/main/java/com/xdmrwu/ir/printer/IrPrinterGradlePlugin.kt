package com.xdmrwu.ir.printer

import java.io.File
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

class IrPrinterGradlePlugin : KotlinCompilerPluginSupportPlugin {
    override fun apply(target: Project) {
        target.extensions.create("ir-printer", IrPrinterExtension::class.java)
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        return true
    }

    override fun getCompilerPluginId(): String = "io.github.xdmrwu.ir.printer"

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = "io.github.xdmrwu",
        artifactId = "ir-printer-compiler-plugin",
        version = "0.0.1"
    )

    override fun applyToCompilation(
        kotlinCompilation: KotlinCompilation<*>
    ): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project
        val extension = project.extensions.getByType(IrPrinterExtension::class.java)

        val options = ArrayList<SubpluginOption>()
        options += SubpluginOption("dumpRawIr", "${extension.dumpRawIr}")
        options += SubpluginOption("dumpComposeStyleIr", "${extension.dumpComposeStyleIr}")
        options += SubpluginOption("buildDir", project.buildDir.absolutePath)
        return project.provider { options }
    }
}
