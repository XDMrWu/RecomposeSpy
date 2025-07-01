package com.xdmrwu.recompose.spy.demo.case.compiler

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MovableContent
import androidx.compose.runtime.key

/**
 * @Author: wulinpeng
 * @Date: 2025/6/30 23:45
 * @Description:
 */
@Composable
fun ReplaceGroupTest() {
    val a = System.currentTimeMillis() > 100

    if (a) {
        Group1()
    } else {
        Group2()
    }
}

@Composable
fun MovableGroup() {
    val a = System.currentTimeMillis() > 100

    key(100) {
        Group1()
    }
    key(a) {
        Group2()
    }
}

@Composable
private fun Group1() {

}

@Composable
private fun Group2() {

}