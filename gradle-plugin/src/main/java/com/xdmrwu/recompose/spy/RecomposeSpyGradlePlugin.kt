package com.xdmrwu.recompose.spy

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

class RecomposeSpyGradlePlugin : KotlinCompilerPluginSupportPlugin {

    override fun apply(target: Project) {
        target.extensions.create("RecomposeSpy", RecomposeSpyExtension::class.java)
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        return true
    }

    override fun getCompilerPluginId(): String = "io.github.xdmrwu.recompose.spy"

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = "io.github.xdmrwu",
        artifactId = "recompose-spy-compiler-plugin",
        version = "0.0.1"
    )

    override fun applyToCompilation(
        kotlinCompilation: KotlinCompilation<*>
    ): Provider<List<SubpluginOption>> {
        kotlinCompilation.dependencies {
            implementation("io.github.xdmrwu:recompose-spy-runtime:0.0.1")
        }

        val project = kotlinCompilation.target.project

        val extension = project.extensions.getByType(RecomposeSpyExtension::class.java)

        val options = ArrayList<SubpluginOption>()
        options += SubpluginOption("dumpRawIr", "${extension.dumpRawIr}")
        options += SubpluginOption("dumpComposeStyleIr", "${extension.dumpComposeStyleIr}")
        options += SubpluginOption("buildDir", project.buildDir.absolutePath)
        return project.provider { options }
    }
}
