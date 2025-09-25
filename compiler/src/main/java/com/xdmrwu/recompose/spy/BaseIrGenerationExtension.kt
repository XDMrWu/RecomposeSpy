package com.xdmrwu.recompose.spy

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.createTmpVariable
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetField
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.expressions.IrBlock
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.expressions.IrStatementContainer
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.isFakeOverride
import org.jetbrains.kotlin.ir.util.isPropertyAccessor
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.propertyIfAccessor
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * @Author: wulinpeng
 * @Date: 2025/6/19 22:21
 * @Description:
 */
abstract class BaseIrGenerationExtension: IrGenerationExtension {
    companion object {
        const val COMPOSABLE_FQ_NAME = "androidx.compose.runtime.Composable"
        const val NON_SKIPPABLE_COMPOSABLE_FQ_NAME = "androidx.compose.runtime.NonSkippableComposable"
        const val NON_RESTARTABLE_COMPOSABLE_FQ_NAME = "androidx.compose.runtime.NonRestartableComposable"
        const val DIRTIES_VAR_NAME = "${"$"}dirties"
        const val DEFAULT_BIT_MASKS_VAR_NAME = "${"$"}defaultBitMasks"
        const val PARAM_NAMES_VAR_NAME = "${"$"}paramNames"
        const val UNUSED_PARAM_NAMES_VAR_NAME = "${"$"}unusedParamNames"
        const val READ_STATE_VAR_NAME = "${"$"}readState"
        const val READ_COMPOSITION_LOCALS_VAR_NAME = "${"$"}readCompositionLocals"
        const val RETURN_VAR_NAME = "${"$"}returnVar"
        const val RECOMPOSE_SPY_PACKAGE = "com.xdmrwu.recompose.spy.runtime"
        const val RECOMPOSE_SPY_CLASS_NAME = "RecomposeSpy"
        const val RECOMPOSE_SPY_START_FUN_NAME = "StartRecomposeSpy"
        const val RECOMPOSE_SPY_END_FUN_NAME = "EndRecomposeSpy"
        const val RECOMPOSE_SPY_GET_EMPTY_DIRTIES_FUN_NAME = "getEmptyDirties"
        const val RECOMPOSE_SPY_RECORD_READ_VALUE_FUN_NAME = "recordReadValue"
        val STATE_CLASS_NAMES = listOf(
            "androidx.compose.runtime.State",
            "androidx.compose.runtime.MutableState"
        )
        val COMPOSITION_LOCAL_CLASS_NAMES = listOf(
            "androidx.compose.runtime.CompositionLocal",
            "androidx.compose.runtime.ProvidableCompositionLocal",
            "androidx.compose.runtime.StaticProvidableCompositionLocal",
            "androidx.compose.runtime.DynamicProvidableCompositionLocal",
            "androidx.compose.runtime.ComputedProvidableCompositionLocal"
        )
        val DISPATCH_RECEIVER_THIS_PARAM_NAME = "<dispatch-this>"
        val EXTENSION_RECEIVER_THIS_PARAM_NAME = "<extension-this>"
    }

    protected fun IrFunction.ignore(): Boolean {
        return !isComposable()
                || body == null
                || body !is IrBlockBody
                || isFakeOverride
                || file.packageFqName.asString() == RECOMPOSE_SPY_PACKAGE // 忽略工具包中的函数
    }

    fun IrFunction.isComposable(): Boolean {
        // 检查函数是否有 @Composable 注解
        return this.hasAnnotation(FqName(COMPOSABLE_FQ_NAME))
    }

    fun IrFunction.nonSkippable(): Boolean {
        // 检查函数是否有 @Composable 注解
        return this.hasAnnotation(FqName(NON_SKIPPABLE_COMPOSABLE_FQ_NAME))
    }

    fun IrFunction.nonRestartable(): Boolean {
        // 检查函数是否有 @Composable 注解
        return this.hasAnnotation(FqName(NON_RESTARTABLE_COMPOSABLE_FQ_NAME))
    }

    fun IrFunction.getStartLine(): Int {
        // 获取函数所在的行号
        return this.file.fileEntry.getLineNumber(this.startOffset) + 1
    }

    fun IrFunction.getEndLine(): Int {
        // 获取函数结束所在的行号
        return this.file.fileEntry.getLineNumber(this.endOffset) + 1
    }

    fun irCall(pluginContext: IrPluginContext, irBuilder: DeclarationIrBuilder,
               packageName: String, className: String?, functionName: String,
               filter: (IrFunctionSymbol) -> Boolean = { true }): IrCall {
        val functionSymbol = pluginContext.referenceFunctions(
            CallableId(
                FqName(packageName),
                className?.let { FqName(it) },
                Name.identifier(functionName)
            )
        ).first {
            filter(it)
        }
        return irBuilder.irCall(functionSymbol)
    }

    // TODO 属性名称
    fun IrCall.tryGetStateReadCallName(irBuilder: DeclarationIrBuilder): IrExpression? {
        // 判断 MutableState / State 的 <get-value> 函数调用
        if (symbol.owner.name.asString() == "<get-value>"
            && symbol.owner.dispatchReceiverParameter?.type?.classFqName?.asString() in STATE_CLASS_NAMES) {
            // TODO 某些场景拿不到名称，需要优化逻辑，同时支持 by
            return dispatchReceiver
        }
        // 判断对委托属性的调用
        if (symbol.owner.isPropertyAccessor
            && symbol.owner.propertyIfAccessor is IrProperty) {
            val irProperty = symbol.owner.propertyIfAccessor as IrProperty
            var isStateReadCall = false
            if (irProperty.backingField != null && irProperty.isDelegated) {
                isStateReadCall =  irProperty.backingField!!.type.classFqName?.asString() in STATE_CLASS_NAMES
            }
            if (isStateReadCall) {
                return irBuilder.irGetField(dispatchReceiver, irProperty.backingField!!)
            } else {
                return null
            }
        }
        return null
    }

    fun IrCall.tryGetCompositionLocalReadCallName(): IrExpression? {
        // 判断 MutableState / State 的 <get-value> 函数调用
        if (symbol.owner.name.asString() == "<get-current>"
            && symbol.owner.dispatchReceiverParameter?.type?.classFqName?.asString() in COMPOSITION_LOCAL_CLASS_NAMES) {
            return dispatchReceiver
        }
        return null
    }

    fun IrExpression.getPropertyName(): String? {
        // 获取表达式中调用的属性名称
        // TODO 更多场景
        return when (this) {
            is IrCall -> {
                if (symbol.owner.isPropertyAccessor && symbol.owner.propertyIfAccessor is IrProperty) {
                    val irProperty = symbol.owner.propertyIfAccessor as IrProperty
                    irProperty.name.asString()
                } else {
                    null
                }
            }
            else -> null
        }
    }

    fun IrFunction.addStatementBeforeReturn(irBuilder: DeclarationIrBuilder, statementGenerator: () -> List<IrStatement>) {
        (body as? IrBlockBody) ?: error("Function body is not a block body, ${kotlinFqName.asString()}")
        val irReturns = mutableListOf<Pair<IrReturn, IrStatementContainer>>()

        val irBlockStack = mutableListOf<IrStatementContainer>()
        irBlockStack.add(body as IrBlockBody)

        acceptChildrenVoid(object : NestedFunctionAwareVisitor() {

            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }

            override fun visitBlock(expression: IrBlock) {
                if (isNestedScope) {
                    super.visitBlock(expression)
                    return
                }
                irBlockStack.add(expression)
                super.visitBlock(expression)
                irBlockStack.remove(expression)
            }

            override fun visitReturn(expression: IrReturn) {
                if (isNestedScope) {
                    super.visitReturn(expression)
                    return
                }
                val index = irBlockStack.last().statements.indexOf(expression)
                if (index < 0) {
                    error("Return expression not found in the current body stack")
                }
                irReturns.add(expression to irBlockStack.last())
                super.visitReturn(expression)
            }
        })

        irBlockStack.remove(body as IrBlockBody)

        irReturns.forEach {
            val irReturn = it.first
            val body = it.second
            val statements = body?.statements ?: return@forEach

            val returnVar = irBuilder.scope.createTmpVariable(
                irReturn.value.type,
                RETURN_VAR_NAME,
                initializer = irReturn.value
            )
            val newIrReturn = irBuilder.irReturn(
                irBuilder.irGet(returnVar)
            )

            val index = statements.indexOf(irReturn)

            statements.remove(irReturn)
            statements.addAll(index, listOf(returnVar, *statementGenerator().toTypedArray(), newIrReturn))
        }

        if (irReturns.isEmpty()) {
            (body as? IrBlockBody)?.statements?.addAll(statementGenerator())
        }
    }

    fun IrCall.putValueArguments(vararg valueArgument: IrExpression?) {
        valueArgument.forEachIndexed { index, irExpression ->
            putValueArgument(index, irExpression)
        }
    }
}

abstract class NestedFunctionAwareVisitor: IrElementVisitorVoid {
    var isNestedScope = false
    final override fun visitFunction(declaration: IrFunction) {
        val wasNested = isNestedScope
        try {
            isNestedScope = true
            return super.visitFunction(declaration)
        } finally {
            isNestedScope = wasNested
        }
    }
}

abstract class NestedFunctionAwareTransformer: IrElementTransformerVoid() {
    var isNestedScope = false

    override fun visitFunction(declaration: IrFunction): IrStatement {
        val wasNested = isNestedScope
        try {
            isNestedScope = true
            return super.visitFunction(declaration)
        } finally {
            isNestedScope = wasNested
        }
    }
}