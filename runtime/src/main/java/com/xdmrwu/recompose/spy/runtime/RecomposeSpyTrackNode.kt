package com.xdmrwu.recompose.spy.runtime

import kotlinx.serialization.Serializable

/**
 * @Author: wulinpeng
 * @Date: 2025/6/26 21:08
 * @Description:
 */
@Serializable
class RecomposeSpyTrackNode(
    val fqName: String,
    val file: String,
    val startLine: Int,
    val endLine: Int,
    val startOffset: Int,
    val endOffset: Int,
    val hasDispatchReceiver: Boolean,
    val hasExtensionReceiver: Boolean,
    val isLambda: Boolean = false,
    val inline: Boolean = false,
    val hasReturnType: Boolean = false, // 有返回值的Composable 不会是 restartable，也不会 skip
    val nonSkippable: Boolean = false,
    val nonRestartable: Boolean = false,
    val children: MutableList<RecomposeSpyTrackNode> = mutableListOf()
) {

    lateinit var recomposeState: RecomposeState

    fun addChild(child: RecomposeSpyTrackNode) {
        children.add(child)
    }

    override fun toString(): String {
        return "${file.last('/')}.${fqName.last()}[$startLine:$endLine] " +
                "(hasDispatchReceiver=$hasDispatchReceiver, hasExtensionReceiver=$hasExtensionReceiver, isLambda=$isLambda, inline=$inline, hasReturnType=$hasReturnType, nonSkippable=$nonSkippable, nonRestartable=$nonRestartable), $recomposeState"
    }

    fun generateSpyInfo(indent: String = ""): String {
        val sb = StringBuilder()
        sb.append("$indent${toString()}\n")
        for (child in children) {
            sb.append(child.generateSpyInfo(indent + "  "))
        }
        return sb.toString()
    }

    private fun String.last(split: Char = '.'): String {
        return split(split).last()
    }
}
@Serializable
data class RecomposeState(val paramStates: List<RecomposeParamState>,
                          val readStates: List<RecomposeReadState>,
                          val readCompositionLocals: List<RecomposeReadState>,
                          val forceRecompose: Boolean = false)

@Serializable
data class RecomposeParamState(
    val name: String,
    val used: Boolean,
    val static: Boolean = false,
    val changed: Boolean = false,
    val uncertain: Boolean = false, // TODO default 参数 dirty会被重置为 uncertain
    val useDefaultValue: Boolean = false,
)

@Serializable
data class RecomposeReadState(
    val name: String,
    val oldValue: String?,
    val newValue: String?,
    val changed: Boolean
)