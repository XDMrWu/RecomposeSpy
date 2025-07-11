package com.xdmrwu.recompose.spy.runtime.printer

import com.xdmrwu.recompose.spy.runtime.RecomposeSpyTrackNode

/**
 * @Author: wulinpeng
 * @Date: 2025/7/3 21:26
 * @Description:
 */
class LogReporter: IRecomposeSpyReporter {

    override fun onRecompose(node: RecomposeSpyTrackNode) {
        println(node.generateSpyInfo().lines().joinToString("\n") {
            "[RecomposeSpy] $it"
        })
    }
}