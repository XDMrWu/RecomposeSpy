package com.xdmrwu.recompose.spy

import com.google.auto.service.AutoService
import com.xdmrwu.recompose.spy.ir.IrPrinterExtension
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration


@ExperimentalCompilerApi
@AutoService(CompilerPluginRegistrar::class)
class RecomposeSpyCompilerPluginRegistrar : CompilerPluginRegistrar() {

    private val extension = RecomposeSpyExtension()
    private val afterComposeExtension = RecomposeSpyAfterComposeExtension()
    private val irPrinterExtension = IrPrinterExtension()

    override val supportsK2: Boolean = true

    // TODO 自动依赖 runtime
    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        val extensionsList = getExtensionsList()
        // 插入到最前面，保证在 Compose Compiler 前执行
        extensionsList.add(0, extension)
        extensionsList.add(afterComposeExtension)
        extensionsList.add(irPrinterExtension)
    }

    private fun ExtensionStorage.getExtensionsList(): MutableList<IrGenerationExtension> {
        val map = ExtensionStorage::class.java.getDeclaredField("_registeredExtensions").apply {
            isAccessible = true
        }.get(this) as MutableMap<Any, MutableList<Any>>
        return map.getOrPut(IrGenerationExtension, ::mutableListOf) as MutableList<IrGenerationExtension>
    }
}
