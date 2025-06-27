package com.xdmrwu.recompose.spy

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.backend.js.utils.valueArguments
import org.jetbrains.kotlin.ir.builders.createTmpVariable
import org.jetbrains.kotlin.ir.builders.irBoolean
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.builders.irVararg
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.isVararg
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * @Author: wulinpeng
 * @Date: 2025/6/16 23:27
 * @Description:
 */
class RecomposeSpyExtension: BaseExtension() {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        moduleFragment.transformChildrenVoid(object : IrElementTransformerVoid() {
            // 识别最外层的 @Composable 方法
            override fun visitFunction(declaration: IrFunction): IrStatement {
                if (declaration.ignore()) {
                    return super.visitFunction(declaration)
                }

                insertCalls(pluginContext, declaration)

                return super.visitFunction(declaration)
            }

            // 识别@Composable lambda 方法
            override fun visitCall(expression: IrCall): IrExpression {
                // 在 IR 阶段，类似 Box { ... } 语法实际上会被编译为一个 IrCall
                // 其中包含一个 lambda 作为参数，这个 lambda 会变成一个 IrFunctionExpression，其内部是一个匿名的 IrFunction。
                // Box(content: @Composable () -> Unit) {
                // @Composable 不是参数本身的注解，而是参数类型的 FunctionType 的 invoke() 方法带有 @Composable。
                val function = expression.symbol.owner
                expression.valueArguments.filterIsInstance<IrFunctionExpression>().forEach {
                    val type = it.type
                    val clazz = type.classOrNull?.owner ?: return@forEach
                    val isComposableLambda = clazz.functions.firstOrNull {
                        it.name.asString() == "invoke"
                    }?.isComposable() ?: false
                    if (isComposableLambda) {
                        // 如果是一个 inline 方法 call 的 Composable lambda 参数，这个参数不会有 changed
                        insertCalls(pluginContext, it.function, function.isInline)
                    }
                }
                return super.visitCall(expression)
            }
        })
    }

    private fun insertCalls(pluginContext: IrPluginContext, irFunction: IrFunction,
                            isInlineLambdaParam: Boolean = false) {
        val irBuilder = DeclarationIrBuilder(pluginContext, irFunction.symbol)
        insertStartCall(pluginContext, irBuilder, irFunction, isInlineLambdaParam)
        insertEndCall(pluginContext, irBuilder, irFunction)
    }

    private fun insertStartCall(pluginContext: IrPluginContext, irBuilder: DeclarationIrBuilder,
                                declaration: IrFunction, isInlineLambdaParam: Boolean) {
        val fqName = irBuilder.irString(declaration.kotlinFqName.asString())
        val fileName = irBuilder.irString(declaration.file.fileEntry.name)
        val startLine = irBuilder.irInt(declaration.getStartLine())
        val endLine = irBuilder.irInt(declaration.getEndLine())
        val inline = irBuilder.irBoolean(isInlineLambdaParam || declaration.isInline)
        val nonSkippable = irBuilder.irBoolean(declaration.nonSkippable())
        val nonRestartable = irBuilder.irBoolean(declaration.nonRestartable())

        val startCall = irCall(pluginContext, irBuilder, RECOMPOSE_SPY_PACKAGE, RECOMPOSE_SPY_CLASS_NAME, RECOMPOSE_SPY_START_FUN_NAME).apply {
            dispatchReceiver = irBuilder.irGetObject(
                pluginContext.referenceClass(
                    ClassId(
                        FqName(RECOMPOSE_SPY_PACKAGE), Name.identifier(RECOMPOSE_SPY_CLASS_NAME)
                    )
                )!!
            )
            putValueArgument(0, fqName)
            putValueArgument(1, fileName)
            putValueArgument(2, startLine)
            putValueArgument(3, endLine)
            putValueArgument(4, inline)
            putValueArgument(5, nonSkippable)
            putValueArgument(6, nonRestartable)
        }

        (declaration.body as IrBlockBody).statements.add(0, startCall)
    }

    private fun insertEndCall(pluginContext: IrPluginContext, irBuilder: DeclarationIrBuilder, declaration: IrFunction) {
        val dirties = getDirties(pluginContext, irBuilder, declaration)
        val paramNames = getParamNames(pluginContext, irBuilder, declaration)
        val unusedParamNames = getUnusedParamNames(pluginContext, irBuilder, declaration)
        val readStates = getReadStates(pluginContext, irBuilder, declaration)
        val readCompositionLocals = getReadCompositionLocals(pluginContext, irBuilder, declaration)

        val endCall = irCall(pluginContext, irBuilder, RECOMPOSE_SPY_PACKAGE, RECOMPOSE_SPY_CLASS_NAME, RECOMPOSE_SPY_END_FUN_NAME).apply {
            dispatchReceiver = irBuilder.irGetObject(
                pluginContext.referenceClass(
                    ClassId(
                        FqName(RECOMPOSE_SPY_PACKAGE), Name.identifier(RECOMPOSE_SPY_CLASS_NAME)
                    )
                )!!
            )
            putValueArgument(0, irBuilder.irGet(dirties))
            putValueArgument(1, irBuilder.irGet(paramNames))
            putValueArgument(2, irBuilder.irGet(unusedParamNames))
            putValueArgument(3, irBuilder.irGet(readStates))
            putValueArgument(4, irBuilder.irGet(readCompositionLocals))
        }

        (declaration.body as IrBlockBody).statements.add(endCall)
    }


    private fun getDirties(pluginContext: IrPluginContext, irBuilder: DeclarationIrBuilder, declaration: IrFunction): IrVariable {
        val getDirtiesCall = irCall(pluginContext, irBuilder, RECOMPOSE_SPY_PACKAGE, null, RECOMPOSE_SPY_GET_EMPTY_DIRTIES_FUN_NAME)
        val variable = irBuilder.scope.createTmpVariable(
            pluginContext.irBuiltIns.arrayClass.owner.defaultType,
            DIRTIES_VAR_NAME,
            initializer = getDirtiesCall
        )
        (declaration.body as IrBlockBody).statements.add(variable)
        return variable
    }

    private fun getParamNames(pluginContext: IrPluginContext, irBuilder: DeclarationIrBuilder, declaration: IrFunction): IrVariable {
        val arrayOfCall = irCall(pluginContext, irBuilder, "kotlin", null, "arrayOf") {
            it.owner.valueParameters.any { it.isVararg }
        }.apply {
            putTypeArgument(0, pluginContext.irBuiltIns.stringType)
            putValueArgument(0, irBuilder.irVararg(pluginContext.irBuiltIns.stringType, declaration.valueParameters.map {
                irBuilder.irString(it.name.asString())
            }))
        }

        val variable = irBuilder.scope.createTmpVariable(
            pluginContext.irBuiltIns.arrayClass.owner.defaultType,
            "paramNames",
            initializer = arrayOfCall
        )
        (declaration.body as IrBlockBody).statements.add(variable)

        return variable
    }

    // TODO lambda 内使用的参数判断
    private fun getUnusedParamNames(pluginContext: IrPluginContext, irBuilder: DeclarationIrBuilder, declaration: IrFunction): IrVariable {

        val unusedParams = declaration.valueParameters.toMutableList()

        declaration.body?.acceptChildrenVoid(object : IrElementVisitorVoid {

            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }

            override fun visitGetValue(expression: IrGetValue) {
                unusedParams.removeIf { it.symbol == expression.symbol }
                super.visitGetValue(expression)
            }
        })

        val arrayOfCall = irCall(pluginContext, irBuilder, "kotlin", null, "arrayOf") {
            it.owner.valueParameters.any { it.isVararg }
        }.apply {
            putTypeArgument(0, pluginContext.irBuiltIns.stringType)
            putValueArgument(0, irBuilder.irVararg(pluginContext.irBuiltIns.stringType, unusedParams.map {
                irBuilder.irString(it.name.asString())
            }))
        }

        val variable = irBuilder.scope.createTmpVariable(
            pluginContext.irBuiltIns.arrayClass.owner.defaultType,
            "unusedParamNames",
            initializer = arrayOfCall
        )
        (declaration.body as IrBlockBody).statements.add(variable)

        return variable
    }

    // 获取当前方法中对 State.getValue 的调用
    private fun getReadStates(pluginContext: IrPluginContext, irBuilder: DeclarationIrBuilder, function: IrFunction): IrVariable {

        val mapOfCall = irCall(pluginContext, irBuilder, "kotlin.collections", null, "mutableMapOf"){
            it.owner.valueParameters.any { it.isVararg }
        }.apply {
            putTypeArgument(0, pluginContext.irBuiltIns.stringType)
            putTypeArgument(1, pluginContext.irBuiltIns.anyNType)
        }

        val readStateVariable = irBuilder.scope.createTmpVariable(
            pluginContext.irBuiltIns.mutableMapClass.owner.defaultType,
            "readState",
            initializer = mapOfCall
        )

        (function.body as IrBlockBody).statements.add(0, readStateVariable)

        // 获取 state 的地方插入 recordReadValue
        function.body?.transformChildrenVoid(object : NestedFunctionAwareTransformer() {
            override fun visitCall(expression: IrCall): IrExpression {
                if (isNestedScope) {
                    return super.visitCall(expression)
                }
                val statePropertyName = expression.tryGetStateReadCallName()
                if (statePropertyName != null) {
                    val recordValueCall = irCall(pluginContext, irBuilder, RECOMPOSE_SPY_PACKAGE, null, RECOMPOSE_SPY_RECORD_READ_VALUE_FUN_NAME).apply {
                        putTypeArgument(0, expression.type)
                        putValueArgument(0, irBuilder.irGet(readStateVariable))
                        putValueArgument(1, irBuilder.irString(statePropertyName))
                        putValueArgument(2, expression)
                    }

                    return recordValueCall
                }
                return super.visitCall(expression)
            }
        })

        return readStateVariable
    }

    // 获取当前方法中对 CompositionLocal.current 的调用
    private fun getReadCompositionLocals(pluginContext: IrPluginContext, irBuilder: DeclarationIrBuilder, function: IrFunction): IrVariable {
        val mapOfCall = irCall(pluginContext, irBuilder, "kotlin.collections", null, "mutableMapOf") {
            it.owner.valueParameters.any { it.isVararg }
        }.apply {
            putTypeArgument(0, pluginContext.irBuiltIns.stringType)
            putTypeArgument(1, pluginContext.irBuiltIns.anyNType)
        }

        val readCompositionLocalsVariable = irBuilder.scope.createTmpVariable(
            pluginContext.irBuiltIns.mutableMapClass.owner.defaultType,
            "readCompositionLocals",
            initializer = mapOfCall
        )

        (function.body as IrBlockBody).statements.add(0, readCompositionLocalsVariable)

        // 获取 compositionLocal 的地方插入 recordReadValue
        function.body?.transformChildrenVoid(object : NestedFunctionAwareTransformer() {
            override fun visitCall(expression: IrCall): IrExpression {
                if (isNestedScope) {
                    return super.visitCall(expression)
                }
                val compositionLocalPropertyName = expression.tryGetCompositionLocalReadCallName()
                if (compositionLocalPropertyName != null) {
                    val recordValueCall = irCall(pluginContext, irBuilder, RECOMPOSE_SPY_PACKAGE, null, RECOMPOSE_SPY_RECORD_READ_VALUE_FUN_NAME).apply {
                        putTypeArgument(0, expression.type)
                        putValueArgument(0, irBuilder.irGet(readCompositionLocalsVariable))
                        putValueArgument(1, irBuilder.irString(compositionLocalPropertyName))
                        putValueArgument(2, expression)
                    }
                    return recordValueCall
                }
                return super.visitCall(expression)
            }
        })

        return readCompositionLocalsVariable
    }
}