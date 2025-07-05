package com.xdmrwu.recompose.spy.demo.case.compiler

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember

/**
 * @Author: wulinpeng
 * @Date: 2025/7/5 20:32
 * @Description:
 */
@Stable
open class OuterClass(var name: String) {
    @Composable
    fun ThisParamTest(name: String) {
        println(name)
    }

    @Composable
    fun ThisPramTestWithCapture(name: String) {
        // changed 最后一个参数表示 this
        println(name)
        println(this.toString())
    }

    @Composable
    open fun ThisPramTestWithCaptureOpen(name: String) {
        // open 方法没有 restart & skip 逻辑
        println(name)
        println(this.toString())
    }
}

@Composable
fun ThisParamTestCase() {
    // 传递的 this changed 都是 00，uncertain
    val outer = OuterClass("")
    outer.ThisParamTest("OuterClass")
    outer.ThisPramTestWithCapture("OuterClass")
    outer.ThisPramTestWithCaptureOpen("OuterClass")
}

@Composable
fun ThisParamTestCaseWithRemember() {
    // OuterClass是 stable: 传递的 this changed 都是 011，static
    // OuterClass非 stable: 传递的 this changed 都是 00，uncertain
    val outer = remember { OuterClass("") }
    outer.ThisParamTest("OuterClass")
    outer.ThisPramTestWithCapture("OuterClass")
    outer.ThisPramTestWithCaptureOpen("OuterClass")
}