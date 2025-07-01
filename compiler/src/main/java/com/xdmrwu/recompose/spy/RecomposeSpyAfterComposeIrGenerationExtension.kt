package com.xdmrwu.recompose.spy

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irVararg
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.types.isArray
import org.jetbrains.kotlin.ir.types.isInt
import org.jetbrains.kotlin.ir.util.isVararg
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * @Author: wulinpeng
 * @Date: 2025/6/19 22:13
 * @Description:
 */
class RecomposeSpyAfterComposeIrGenerationExtension: BaseIrGenerationExtension() {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        moduleFragment.transformChildrenVoid(object : IrElementTransformerVoid() {
            // 经过 ComposeCompiler 处理，不需要单独关注 lambda，只需要关注 IrFunction
            override fun visitFunction(declaration: IrFunction): IrStatement {
                if (declaration.ignore()) {
                    return super.visitFunction(declaration)
                }

                val irBuilder = DeclarationIrBuilder(pluginContext, declaration.symbol)
                replaceDirties(pluginContext, irBuilder, declaration)
                replaceDefaultBitMasks(pluginContext, irBuilder, declaration)

                return super.visitFunction(declaration)
            }
        })
    }

    private fun replaceDirties(pluginContext: IrPluginContext, irBuilder: DeclarationIrBuilder, declaration: IrFunction) {
        val findDirtiesVariable = findDirtiesVariable(pluginContext, irBuilder, declaration)
        if (findDirtiesVariable.isEmpty()) {
            // Compose 生成的匿名方法没有提前插桩，跳过
            return
        }
        val dirtyFlags = findDirtyFlags(pluginContext, irBuilder, declaration)
        replaceGetDirtiesCall(pluginContext, irBuilder, declaration, dirtyFlags, findDirtiesVariable)
    }

    private fun findDirtyFlags(pluginContext: IrPluginContext, irBuilder: DeclarationIrBuilder, declaration: IrFunction): List<IrVariable> {

        val dirtyFlags = mutableListOf<IrVariable>()

        declaration.body?.acceptChildrenVoid(object : NestedFunctionAwareVisitor() {

            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }

            override fun visitVariable(declaration: IrVariable) {
                if (isNestedScope) {
                    // 不处理嵌套方法，比如 lambda
                    return super.visitVariable(declaration)
                }
                val variableName = declaration.name.asString()
                if (variableName.startsWith("${"$"}dirty") && declaration.type.isInt()) {
                    dirtyFlags.add(declaration)
                }
                super.visitVariable(declaration)
            }
        })

        // 删除非数字结尾的变量
        // TODO 顺序是否一致
        dirtyFlags.removeIf { variable ->
            val variableName = variable.name.asString()
            variableName != "${"$"}dirty"
                    && variableName.removePrefix("${"$"}dirty").toIntOrNull() == null
        }

        return dirtyFlags
    }

    private fun findDirtiesVariable(pluginContext: IrPluginContext, irBuilder: DeclarationIrBuilder, declaration: IrFunction): List<IrVariable> {

        var dirties = mutableListOf<IrVariable>()

        declaration.body?.acceptChildrenVoid(object : NestedFunctionAwareVisitor() {

            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }

            override fun visitVariable(declaration: IrVariable) {
                if (isNestedScope) {
                    // 不处理嵌套方法，比如 lambda
                    return super.visitVariable(declaration)
                }
                val variableName = declaration.name.asString()
                if (variableName == DIRTIES_VAR_NAME && declaration.type.isArray()) {
                    dirties.add(declaration)
                }
                super.visitVariable(declaration)
            }
        })

        return dirties
    }

    private fun replaceGetDirtiesCall(pluginContext: IrPluginContext, irBuilder: DeclarationIrBuilder,
                                      function: IrFunction, dirtyFlags: List<IrVariable>, findDirtiesVariables: List<IrVariable>) {
        findDirtiesVariables.forEach {
            val arrayOfSymbol = pluginContext.referenceFunctions(CallableId(FqName("kotlin"), null, Name.identifier("arrayOf")))
                .firstOrNull { funcSymbol ->
                    funcSymbol.owner.valueParameters.any { it.isVararg }
                }
            val params = if (dirtyFlags.isNotEmpty()) {
                dirtyFlags.map { irBuilder.irGet(it) }
            } else {
                // 如果没有 dirtyFlags，则使用@Composable 的 $changed 参数
                listOf(irBuilder.irGet(function.valueParameters.first {it.name.asString() == "${"$"}changed"}))
            }
            val arrayOfCall = irBuilder.irCall(arrayOfSymbol!!).apply {
                putTypeArgument(0, pluginContext.irBuiltIns.intType)
                putValueArgument(0, irBuilder.irVararg(pluginContext.irBuiltIns.intType, params))
            }
            it.initializer = arrayOfCall
        }
    }

    private fun replaceDefaultBitMasks(pluginContext: IrPluginContext, irBuilder: DeclarationIrBuilder, declaration: IrFunction) {
        val variable = findDefaultBitMasksVariable(pluginContext, irBuilder, declaration) ?: return
        variable.initializer = irCall(pluginContext, irBuilder, "kotlin", null, "arrayOf") {
            it.owner.valueParameters.any { it.isVararg }
        }.apply {
            putTypeArgument(0, pluginContext.irBuiltIns.intType)
            putValueArgument(0, irBuilder.irVararg(pluginContext.irBuiltIns.intType, declaration.valueParameters.filter {
                it.name.asString().startsWith("${"$"}default")
            }.map {
                irBuilder.irGet(it)
            }))
        }

    }

    private fun findDefaultBitMasksVariable(pluginContext: IrPluginContext, irBuilder: DeclarationIrBuilder, declaration: IrFunction): IrVariable? {

        var variable: IrVariable? = null

        declaration.body?.acceptChildrenVoid(object : NestedFunctionAwareVisitor() {

            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }

            override fun visitVariable(declaration: IrVariable) {
                if (isNestedScope) {
                    // 不处理嵌套方法，比如 lambda
                    return super.visitVariable(declaration)
                }
                val variableName = declaration.name.asString()
                if (variableName == DEFAULT_BIT_MASKS_VAR_NAME) {
                    variable = declaration
                }
                super.visitVariable(declaration)
            }
        })

        return variable
    }
}