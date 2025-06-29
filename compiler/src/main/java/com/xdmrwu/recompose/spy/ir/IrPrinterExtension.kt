package com.xdmrwu.recompose.spy.ir

import com.xdmrwu.recompose.spy.RecomposeSpyCommandLineOption
import com.xdmrwu.recompose.spy.ir.compose.*
import java.io.File
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.name
import org.jetbrains.kotlin.ir.util.dump

class IrPrinterExtension : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        // 获取 build 目录

        val dumpRawIr = RecomposeSpyCommandLineOption.DumpRawIr.getValue()
        val dumpComposeStyleIr = RecomposeSpyCommandLineOption.DumpComposeStyleIr.getValue()
        val buildDir = File(RecomposeSpyCommandLineOption.BuildDir.getValue())

        moduleFragment.files.forEach { irFile ->

            val path = irFile.packageFqName.asString().replace(".", "/")

            if (dumpRawIr) {
                val dir = buildDir.getOrCreateDir("outputs/ir-printer/raw-ir/$path")
                val file = File(dir, irFile.name)
                file.writeText(irFile.dump())
            }

            if (dumpComposeStyleIr) {
                val dir = buildDir.getOrCreateDir("outputs/ir-printer/compose-style-ir/$path")
                val file = File(dir, irFile.name)
                file.writeText(irFile.dumpSrc())
            }
        }
    }

    private fun File.getOrCreateDir(path: String): File {
        val dir = resolve(path)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }
}
