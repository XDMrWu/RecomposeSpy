package com.xdmrwu.recompose.spy.runtime

/**
 * @Author: wulinpeng
 * @Date: 2025/6/26 21:08
 * @Description:
 */
class RecomposeSpyTrackNode(
    val fqName: String,
    val file: String,
    val startLine: Int,
    val endLine: Int,
    val inline: Boolean = false,
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
                "(inline=$inline, nonSkippable=$nonSkippable, nonRestartable=$nonRestartable), $recomposeState"
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

data class RecomposeState(val paramStates: List<RecomposeParamState>,
                          val readStates: List<RecomposeReadState>,
                          val readCompositionLocals: List<RecomposeReadState>,
                          val forceRecompose: Boolean = false)

data class RecomposeParamState(
    val name: String,
    val used: Boolean,
    val static: Boolean = false,
    val changed: Boolean = false,
    val uncertain: Boolean = false, // TODO default 参数 dirty会被重置为 uncertain
)

data class RecomposeReadState(
    val name: String,
    val oldValue: Any?,
    val newValue: Any?,
    val changed: Boolean
)