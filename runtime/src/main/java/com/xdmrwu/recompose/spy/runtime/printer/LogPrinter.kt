package com.xdmrwu.recompose.spy.runtime.printer

/**
 * @Author: wulinpeng
 * @Date: 2025/7/3 21:26
 * @Description:
 */
class LogPrinter: IRecomposeSpyPrinter {
    override fun printMessage(type: Int, message: String) {
        println(message)
    }
}