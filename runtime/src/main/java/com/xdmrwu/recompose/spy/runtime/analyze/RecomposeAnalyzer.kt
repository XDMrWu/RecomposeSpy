package com.xdmrwu.recompose.spy.runtime.analyze

import com.xdmrwu.recompose.spy.runtime.RecomposeSpyTrackNode

/**
 * @Author: wulinpeng
 * @Date: 2025/7/17 23:16
 * @Description:
 */

fun RecomposeSpyTrackNode.recomposeReason(): String {

    val reason = StringBuilder()
    when {
        nonSkippable -> reason.appendLine("此方法被标记为 @NonSkippableComposable, 无法跳过重组")
        hasReturnType -> reason.appendLine("此方法有返回值, 无法跳过重组")
        inline && !isLambda -> reason.appendLine("此方法为 inline, 无法跳过重组")
        inline && isLambda -> reason.appendLine("此方法是 inline 方法的 Lambda 参数，且该参数没有被标记为 noinline, 无法跳过重组")
        // state or compositionLocal read
        else -> {
            // 如果当前 ComposeTree 里有其他 Compose 也读取了相同 state，那么第一个执行的会是 forceResompose，
            // 其他的就不是 force 了，但是也会执行，所以都需要判断 param 和 state

            // 检查参数参数变化
            val changedParams = changedParams()
            val changedStates = changedStatesInfo()

            if (recomposeState.forceRecompose || changedStates.isNotEmpty()) {
                when {
                    changedParams.isEmpty() && changedStates.isEmpty() -> {
                        reason.appendLine("此方法为本次重组的 Scope，但没有找到触发重组、State 或 CompositionLocal 变化信息。")
                    }
                    else -> {
                        reason.appendLine("此方法为本次重组的 Scope，因为以下 State 或 CompositionLocal 发生了变化而触发重组")
                        reason.appendLine(changedParams)
                        reason.appendLine(changedStates)
                    }

                }
            } else {
                when {
                    changedParams.isEmpty() && changedStates.isEmpty() -> {
                        reason.appendLine("该 Composable 方法无法跳过重组，但没有找到Param、State 或 CompositionLocal 变化信息。")
                    }
                    else -> {
                        reason.appendLine("该 Composable 方法无法跳过重组，因为以下 State 或 CompositionLocal 发生了变化而触发重组")
                        reason.appendLine(changedParams)
                        reason.appendLine(changedStates)
                    }

                }
            }
        }
    }
    return reason.toString()
}

fun RecomposeSpyTrackNode.changedParams(): String {
    val changedParams = recomposeState.paramStates.filter { it.changed }

    val changedInfo = StringBuilder()

    if (changedParams.isNotEmpty()) {
        changedParams.forEach { param ->
            changedInfo.appendLine("  - ${param.name}: used=${param.used}, static=${param.static}, uncertain=${param.uncertain}, useDefaultValue=${param.useDefaultValue}")
        }
    }

    return changedInfo.toString()
}

fun RecomposeSpyTrackNode.changedStatesInfo(): String {
    if (recomposeState.readStates.isEmpty()) {
        return ""
    }
    val changedInfo = StringBuilder()

    changedInfo.appendLine("Changed States:")
    recomposeState.readStates.forEach { state ->
        changedInfo.appendLine("  - ${state.file}(${state.startLine}:${state.endLine})")
    }

    return changedInfo.toString()
}

fun RecomposeSpyTrackNode.traverseInlineChildren(
    action: (RecomposeSpyTrackNode) -> Unit
) {
    action(this)
    children.filter {
        it.inline
    }.forEach {
        action(it)
        it.traverseInlineChildren(action)
    }
}