package com.xdmrwu.recompose.spy.demo.case.analyze

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * @Author: wulinpeng
 * @Date: 2025/10/9 22:57
 * @Description:
 */

private var state1 by mutableStateOf(0)
private var state2 by mutableStateOf(0)

@Composable
fun InlineRecomposeTest() {
    println("[RecomposeReasonTest] RecomposeReasonTest")
    RecomposeReasonV1()
    Column {
        Button(onClick = { state1++ }) {
            Text("update state1")
        }
        Button(onClick = { state2++ }) {
            Text("update state2")
        }
    }
}

@Composable
private fun RecomposeReasonV1() {
    RecomposeReasonV2()
}

@Composable
private inline fun RecomposeReasonV2() {
    println(state1)
    RecomposeReasonV3()
}

@Composable
private inline fun RecomposeReasonV3() {
    println(state2)
}