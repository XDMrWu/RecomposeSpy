package com.xdmrwu.recompose.spy.runtime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonSkippableComposable
import androidx.compose.runtime.remember

/**
 * @Author: wulinpeng
 * @Date: 2025/6/17 22:48
 * @Description:
 */

const val SLOTS_PER_INT = 10
const val BITS_PER_SLOT = 3

private class TrackValueHolder(var value: Map<String, Any?>) {
    operator fun get(key: String): Any? {
        return value[key]
    }

    fun hasKey(key: String): Boolean {
        return value.containsKey(key)
    }
}

object RecomposeSpy {

    private val trackNodeStack = mutableListOf<RecomposeSpyTrackNode>()
    // TODO 假设全局只有一个 Composition

    fun startComposableCall(fqName: String, file: String, startLine: Int, endLine: Int,
                            inline: Boolean, hasReturnType: Boolean = false, nonSkippable: Boolean, nonRestartable: Boolean) {
        val node = RecomposeSpyTrackNode(fqName, file, startLine, endLine, inline, hasReturnType, nonSkippable, nonRestartable)
        trackNodeStack.add(node)
        if (trackNodeStack.size > 1) {
            val parentNode = trackNodeStack[trackNodeStack.size - 2]
            parentNode.addChild(node)
        }
    }

    @NonSkippableComposable
    @Composable
    fun RememberComposeInfo(fqName: String, dirties: Array<Int>,
                            paramNames: Array<String>,
                            unusedParams: Array<String>,
                            readStateMap: Map<String, Any?>,
                            readCompositionLocalMap: Map<String, Any?>
    ) {

        val node = trackNodeStack.removeAt(trackNodeStack.size - 1)
        assert(node.fqName == fqName) {
            "Expected to be called in the same composable call as startComposableCall, but got $fqName instead of ${node.fqName}"
        }

        val oldStates = remember { TrackValueHolder(readStateMap.toMap()) }
        val readStates = readStateMap.mapNotNull {
            val key = it.key
            val newValue = it.value
            if (!oldStates.hasKey(key)) {
                // 本次新增的 key，不处理
                return@mapNotNull null
            }
            val oldValue = oldStates[key]
            // TODO 比较方式不一定准确
            RecomposeReadState(key, oldValue, newValue, newValue != oldValue)
        }
        // 重新创建一个 map，避免数自动同步
        oldStates.value = readStateMap.toMap()

        val oldLocals = remember { TrackValueHolder(readCompositionLocalMap) }
        val readCompositionLocals = readCompositionLocalMap.mapNotNull {
            val key = it.key
            val newValue = it.value
            if (!oldLocals.hasKey(key)) {
                // 本次新增的 key，不处理
                return@mapNotNull null
            }
            val oldValue = oldLocals[key]
            // TODO 比较方式不一定准确
            RecomposeReadState(key, oldValue, newValue, newValue != oldValue)
        }
        oldLocals.value = readCompositionLocalMap


        val paramStates = paramNames.mapIndexed { index, name ->
            if (unusedParams.contains(name)) {
                RecomposeParamState(name, false)
            } else {
                val dirty = dirties[index / SLOTS_PER_INT]
                val valueOfSlot = dirty shr (index % SLOTS_PER_INT * BITS_PER_SLOT + 1) and 0b111
                when (valueOfSlot) {
                    0b001 -> RecomposeParamState(name, true, changed = false) // Same
                    0b010 -> RecomposeParamState(name, true, changed = true) // Different
                    0b011 -> RecomposeParamState(name, true, static = true) // Static
                    // TODO default 参数 dirty会被重置为 uncertain, inline 的changed 也可能是透传下来的 uncertain
                    0b000, 0b100 -> RecomposeParamState(name, true, uncertain = true)
                    else -> error("Unexpected value of slot: $valueOfSlot for param: $name")
                }
            }
        }

        node.recomposeState = RecomposeState(
            paramStates,
            readStates,
            readCompositionLocals,
            // TODO key下面的 movegroup 没有 changed
            dirties.isNotEmpty() && dirties[0] and 0b1 == 0b1 // forceRecompose
        )

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