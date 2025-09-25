@file:OptIn(ExperimentalComposeRuntimeApi::class)

package com.xdmrwu.recompose.spy.runtime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ExperimentalComposeRuntimeApi
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.NonSkippableComposable
import androidx.compose.runtime.RecomposeScope
import androidx.compose.runtime.currentRecomposeScope
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.tooling.SnapshotInstanceObservers
import androidx.compose.runtime.snapshots.tooling.SnapshotObserver
import androidx.compose.runtime.snapshots.tooling.observeSnapshots
import com.xdmrwu.recompose.spy.runtime.printer.IRecomposeSpyReporter
import com.xdmrwu.recompose.spy.runtime.printer.IdePluginReporter
import com.xdmrwu.recompose.spy.runtime.printer.LogReporter

/**
 * @Author: wulinpeng
 * @Date: 2025/6/17 22:48
 * @Description:
 */

const val SLOTS_PER_INT = 10
const val BITS_PER_SLOT = 3

enum class RecomposeSpyTrackScope {
    SCOPE_PROJECT_SOURCE, // 监听项目源码中的 State / CompositionLocal 的读取动作，包含丰富的源码信息
    SCOPE_PROJECT_SOURCE_WITH_STACK_TRACE, // 监听项目源码中的 State / CompositionLocal 的读取动作，包含丰富的源码信息与 stacktrace 信息
    SCOPE_ALL // 监听所有 State / CompositionLocal 的读取动作，但是会缺少其他信息，仅包含 stacktrace
}

class RecomposeSpy {

    companion object {
        lateinit var trackScope: RecomposeSpyTrackScope
        private val recomposeSpyList = mutableSetOf<RecomposeSpy>()
        private val reporters = mutableListOf(
            LogReporter(),
            IdePluginReporter()
        )

        fun init(scope: RecomposeSpyTrackScope) {
            this.trackScope = scope
            if (scope == RecomposeSpyTrackScope.SCOPE_ALL) {
                // 监听所有的 Snapshot
                Snapshot.observeSnapshots(object : SnapshotObserver {
                    override fun onPreCreate(
                        parent: Snapshot?,
                        readonly: Boolean
                    ): SnapshotInstanceObservers? {
                        return SnapshotInstanceObservers(readObserver = {
                            onReadState(RecomposeReadState(it, "", "", -1, -1, -1, -1, RuntimeException().stackTraceToString().lines()))
                        })
                    }
                })
            }
        }

        internal fun onReadState(readState: RecomposeReadState) {
            recomposeSpyList.forEach { spy ->
                spy.onReadState(readState)
            }
        }

        internal fun registerOnReadState(recomposeSpy: RecomposeSpy) {
            recomposeSpyList.add(recomposeSpy)
        }

        internal fun unregisterOnReadState(recomposeSpy: RecomposeSpy) {
            recomposeSpyList.remove(recomposeSpy)
        }

        fun registerReporter(reporter: IRecomposeSpyReporter) {
            reporters += reporter
        }
    }

    init {
        registerOnReadState(this)
    }

    // TODO 假设全局只有一个 Composition
    private val trackNodeStack = mutableListOf<RecomposeSpyTrackNode>()

    var currentInvalidationMap: Map<RecomposeScope, Set<Any>> = emptyMap()
    private var currentRecomposeScopeStack: MutableList<RecomposeScope> = mutableListOf()
    private val stateReadInfoMap = mutableMapOf<RecomposeScope, List<RecomposeReadState>>()

    fun onReadState(readState: RecomposeReadState) {
        currentRecomposeScopeStack.lastOrNull()?.let { scope ->

            val list = stateReadInfoMap.getOrPut(scope) {
                mutableListOf()
            } as MutableList<RecomposeReadState>
            list.indexOfFirst {
                it.state === readState.state
            }.takeIf { it >= 0 }?.let { preIndex ->
                list.removeAt(preIndex)
            }
            list.add(readState)
        }
    }

    @Composable
    @NonSkippableComposable
    @NonRestartableComposable
    fun StartRecomposeSpy(fqName: String, file: String, startLine: Int, endLine: Int, startOffset: Int, endOffset: Int,
                            hasDispatchReceiver: Boolean, hasExtensionReceiver: Boolean,
                            isLambda: Boolean, inline: Boolean,
                            hasReturnType: Boolean = false, nonSkippable: Boolean, nonRestartable: Boolean) {

        currentRecomposeScopeStack.add(currentRecomposeScope)

        val node = RecomposeSpyTrackNode(
            fqName, file, startLine, endLine, startOffset, endOffset, hasDispatchReceiver, hasExtensionReceiver,
            isLambda, inline, hasReturnType, nonSkippable, nonRestartable
        )
        trackNodeStack.add(node)
        if (trackNodeStack.size > 1) {
            val parentNode = trackNodeStack[trackNodeStack.size - 2]
            parentNode.addChild(node)
        }
    }

    @Composable
    @NonSkippableComposable
    @NonRestartableComposable
    fun EndRecomposeSpy(
        fqName: String,
        dirties: Array<Int>,
        paramNames: Array<String>,
        unusedParams: Array<String>,
        defaultBitMasks: Array<Int>,
    ) {

        currentRecomposeScopeStack.removeAt(currentRecomposeScopeStack.size - 1)

        val node = trackNodeStack.removeAt(trackNodeStack.size - 1)
        assert(node.fqName == fqName) {
            "Expected to be called in the same composable call as StartRecomposeSpy, but got $fqName instead of ${node.fqName}"
        }

        val invalidStates = currentInvalidationMap[currentRecomposeScope] ?: emptySet()
        val readStates = stateReadInfoMap[currentRecomposeScope] ?: emptyList()
        val stateChangedInfo = readStates.filter {
            invalidStates.contains(it.state)
        }

        val paramStates = paramNames.mapIndexed { index, name ->
            if (unusedParams.contains(name)) {
                RecomposeParamState(name, false)
            } else if (isDefaultValue(index, defaultBitMasks)) {
                // 默认参数
                RecomposeParamState(name, true, uncertain = true, useDefaultValue = true)
            } else {
                val dirty = dirties[index / SLOTS_PER_INT]
                val valueOfSlot = dirty shr (index % SLOTS_PER_INT * BITS_PER_SLOT + 1) and 0b111
                when (valueOfSlot) {
                    0b001 -> RecomposeParamState(name, true, changed = false) // Same
                    0b010 -> RecomposeParamState(name, true, changed = true) // Different
                    0b011 -> RecomposeParamState(name, true, static = true) // Static
//                     TODO default 参数 dirty会被重置为 uncertain, inline 的changed 也可能是透传下来的 uncertain,不可skip 的也可能
                    0b000, 0b100 -> RecomposeParamState(name, true, uncertain = true)
                    else -> error("Unexpected value of slot: $valueOfSlot for param: $name")
                }
            }
        }

        node.recomposeState = RecomposeState(
            paramStates,
            stateChangedInfo,
            // TODO key下面的 movegroup 没有 changed
            dirties.isNotEmpty() && dirties[0] and 0b1 == 0b1 // forceRecompose
        )

        if (trackNodeStack.isEmpty()) {
            node.fillRecomposeReason()
            reporters.forEach { it.onRecompose(node) }
        }
    }

    private fun isDefaultValue(index: Int, defaultBitMasks: Array<Int>): Boolean {
        val slot = index / 32
        val offset = index % 32
        if (defaultBitMasks.size < slot + 1) {
            return false
        }
        return defaultBitMasks[slot] and (1 shl offset) != 0
    }

    fun dispose() {
        unregisterOnReadState(this)
    }

}

/**
 * for compile time placeholder
 */
fun getEmptyDirties(): Array<Int> {
    return arrayOf()
}

fun <T> recordReadValue(originValue:T, state: Any, file: String, propertyName: String, startLine: Int, endLine: Int, startOffset: Int, endOffset: Int): T {
    if (RecomposeSpy.trackScope == RecomposeSpyTrackScope.SCOPE_ALL) {
        // 通过 Snapshot.observeSnapshots 监听，不走编译期插桩
        return originValue
    }
    val stackTrace = if (RecomposeSpy.trackScope == RecomposeSpyTrackScope.SCOPE_PROJECT_SOURCE_WITH_STACK_TRACE) {
        RuntimeException().stackTraceToString().lines()
    } else {
        emptyList()
    }
    RecomposeSpy.onReadState(RecomposeReadState(state, file, propertyName, startLine, endLine, startOffset, endOffset, stackTrace))
    return originValue
}