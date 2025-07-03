package com.xdmrwu.recompose.spy.runtime.printer

import com.xdmrwu.recompose.spy.runtime.RecomposeSpyTrackNode

/**
 * @Author: wulinpeng
 * @Date: 2025/7/3 21:26
 * @Description:
 */
interface IRecomposeSpyPrinter {
    fun printMessage(message: String)

    fun printTrackNode(node: RecomposeSpyTrackNode)
}