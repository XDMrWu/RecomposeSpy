@file:OptIn(ExperimentalCompilerApi::class)

package com.xdmrwu.recompose.spy

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

/**
 * @Author: wulinpeng
 * @Date: 2025/6/22 22:10
 * @Description:
 */
@AutoService(CommandLineProcessor::class)
class RecomposeSpyCommandLineProcessor: CommandLineProcessor {
    override val pluginId = "io.github.xdmrwu.recompose.spy"
    override val pluginOptions: Collection<AbstractCliOption> = listOf(
        RecomposeSpyCommandLineOption.DumpRawIr,
        RecomposeSpyCommandLineOption.DumpComposeStyleIr,
        RecomposeSpyCommandLineOption.BuildDir
    )

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        (option as? RecomposeSpyCommandLineOption<*>)?.processOption(option, value, configuration)
            ?: super.processOption(option, value, configuration)
    }
}

sealed class RecomposeSpyCommandLineOption<T>(
    override val optionName: String,
    override val valueDescription: String = "",
    override val description: String = "",
    override val required: Boolean = true,
    override val allowMultipleOccurrences: Boolean = false
) : AbstractCliOption {
    abstract fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration)
    abstract fun getValue(): T

    object DumpRawIr : RecomposeSpyCommandLineOption<Boolean>(
        optionName = "dumpRawIr",
        valueDescription = "true|false",
        description = "Dump raw IR to build directory.",
        required = false,
        allowMultipleOccurrences = false
    ) {

        private var value: Boolean = false

        override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
            if (option.optionName == optionName) {
                this.value = value.toBoolean()
            }
        }

        override fun getValue(): Boolean {
            return value
        }
    }

    object DumpComposeStyleIr : RecomposeSpyCommandLineOption<Boolean>(
        optionName = "dumpComposeStyleIr",
        valueDescription = "true|false",
        description = "Dump Compose style IR to build directory.",
        required = false,
        allowMultipleOccurrences = false
    ) {
        private var value: Boolean = false

        override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
            if (option.optionName == optionName) {
                this.value = value.toBoolean()
            }
        }

        override fun getValue(): Boolean {
            return value
        }
    }

    object BuildDir : RecomposeSpyCommandLineOption<String>(
        optionName = "buildDir",
        valueDescription = "path",
        description = "The build directory where the IR files will be dumped.",
        required = true,
        allowMultipleOccurrences = false
    ) {
        private var value: String? = null

        override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
            if (option.optionName == optionName) {
                this.value = value
            }
        }

        override fun getValue(): String {
            return value ?: error("Build directory must be specified.")
        }
    }

}