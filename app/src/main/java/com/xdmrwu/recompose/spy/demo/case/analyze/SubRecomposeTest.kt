package com.xdmrwu.recompose.spy.demo.case.analyze

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * @Author: wulinpeng
 * @Date: 2025/10/9 22:57
 * @Description:
 */

private var state1 by mutableStateOf(false)

@Composable
fun SubRecomposeTest() {
    println(state1)
    val value = state1
    Column {
        Button(onClick = { state1 = !state1 }) {
            Text("update state1")
        }
        LazyColumn {
            if (state1) {
                item {
                    RecomposeReasonV1()
                }
            }
            item {
                RecomposeReasonV2(value)
            }
        }
    }
}

@Composable
private fun RecomposeReasonV1() {
    println()
}

@Composable
private fun RecomposeReasonV2(param: Boolean) {
    println(param)
}