package com.xdmrwu.recompose.spy.runtime.printer

import com.xdmrwu.recompose.spy.runtime.RecomposeSpyTrackNode

/**
 * @Author: wulinpeng
 * @Date: 2025/7/3 21:26
 * @Description:
 */
class LogPrinter: IRecomposeSpyPrinter {
    override fun printMessage(message: String) {
        println(message)
    }

    override fun printTrackNode(node: RecomposeSpyTrackNode) {
        println(node.generateSpyInfo().lines().joinToString("\n") {
            "[RecomposeSpy] $it"
        })
    }
}