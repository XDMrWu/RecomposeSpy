package com.xdmrwu.recompose.spy.runtime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonSkippableComposable
import androidx.compose.runtime.remember

/**
 * @Author: wulinpeng
 * @Date: 2025/6/17 22:48
 * @Description:
 */

private class TrackValueHolder(var value: Map<String, Any?>) {
    operator fun get(key: String): Any? {
        return value[key]
    }

    fun hasKey(key: String): Boolean {
        return value.containsKey(key)
    }
}

object RecomposeSpyTracker {

    private val trackNodeStack = mutableListOf<RecomposeSpyTrackNode>()
    // TODO 假设全局只有一个 Composition

    fun startComposableCall(fqName: String, file: String, startLine: Int, endLine: Int,
                            inline: Boolean, nonSkippable: Boolean, nonRestartable: Boolean) {
        val node = RecomposeSpyTrackNode(fqName, file, startLine, endLine, inline, nonSkippable, nonRestartable)
        trackNodeStack.add(node)
        if (trackNodeStack.size > 1) {
            val parentNode = trackNodeStack[trackNodeStack.size - 2]
            parentNode.addChild(node)
        }
    }

    @NonSkippableComposable
    @Composable
    fun RememberComposeInfo(dirties: Array<Int>,
                            paramNames: Array<String>,
                            unusedParams: Array<String>,
                            readStates: Map<String, Any?>,
                            readCompositionLocals: Map<String, Any?>
    ) {

        val oldStates = remember { TrackValueHolder(readStates.toMap()) }
        val stateInfos = mutableListOf<String>()
        readStates.forEach {
            val key = it.key
            val newValue = it.value
            if (!oldStates.hasKey(key)) {
                // 本次新增的 key，不处理
                return@forEach
            }
            val oldValue = oldStates[key]
            // TODO 比较方式不一定准确
            if (newValue != oldValue) {
                stateInfos.add("state[$key] changed: $oldValue -> $newValue")
            }
        }
        // 重新创建一个 map，避免数自动同步
        oldStates.value = readStates.toMap()

        val oldLocals = remember { TrackValueHolder(readCompositionLocals) }
        val compositionLocalInfos = mutableListOf<String>()
        readCompositionLocals.forEach {
            val key = it.key
            val newValue = it.value
            if (!oldLocals.hasKey(key)) {
                // 本次新增的 key，不处理
                return@forEach
            }
            val oldValue = oldLocals[key]
            // TODO 比较方式不一定准确
            if (newValue != oldValue) {
                compositionLocalInfos.add("CompositionLocal[$key] changed: $oldValue -> $newValue")
            }
        }
        oldLocals.value = readCompositionLocals

        val paramInfos = mutableListOf<String>()
        paramNames.forEachIndexed { index, name ->
            if (unusedParams.contains(name)) {
                paramInfos.add("param[$name]: Unused")
                return@forEachIndexed
            }
            val dirty = dirties[index / SLOTS_PER_INT]
            val valueOfSlot = dirty shr (index % SLOTS_PER_INT * BITS_PER_SLOT + 1) and 0b111
            val state = when (valueOfSlot) {
                0b000 -> "Uncertain"
                0b001 -> "Same"
                0b010 -> "Different"
                0b011 -> "Static"
                0b100 -> "Unknown"
                else -> "Mask"
            }
            paramInfos.add("param[$name]: $state")
        }

        val node = trackNodeStack.removeAt(trackNodeStack.size - 1)
        node.info = "Params: ${paramInfos.joinToString(", ")}, " +
                "States: ${stateInfos.joinToString(", ")}, " +
                "Composition Locals: ${compositionLocalInfos.joinToString(", ")}"

        if (trackNodeStack.isEmpty()) {
            // 结束追踪, TODO
            println(node.generateSpyInfo().lines().joinToString("\n") {
                "[RecomposeSpy] $it"
            })
        }
    }
}

/**
 * for compile time placeholder
 */
fun getEmptyDirties(): Array<Int> {
    return arrayOf()
}

fun <T> recordReadValue(map: MutableMap<String, Any?>, name: String, originValue: T): T {
    map[name] = originValue
    return originValue
}

private class RecomposeSpyTrackNode(
    val fqName: String,
    val file: String,
    val startLine: Int,
    val endLine: Int,
    val inline: Boolean = false,
    val nonSkippable: Boolean = false,
    val nonRestartable: Boolean = false,
    val children: MutableList<RecomposeSpyTrackNode> = mutableListOf()
) {

    var info: String = ""

    fun addChild(child: RecomposeSpyTrackNode) {
        children.add(child)
    }

    override fun toString(): String {
        return "RecomposeSpyTrackNode(fqName='$fqName', file='$file', startLine=$startLine, endLine=$endLine, " +
                "inline=$inline, nonSkippable=$nonSkippable, nonRestartable=$nonRestartable, info='$info')"
    }

    fun generateSpyInfo(indent: String = ""): String {
        val sb = StringBuilder()
        sb.append("$indent${toString()}\n")
        for (child in children) {
            sb.append(child.generateSpyInfo(indent + "  "))
        }
        return sb.toString()
    }
}