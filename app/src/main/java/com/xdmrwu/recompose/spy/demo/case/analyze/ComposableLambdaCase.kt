package com.xdmrwu.recompose.spy.demo.case.analyze

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * @Author: wulinpeng
 * @Date: 2025/10/20 15:11
 * @Description:
 */
@Composable
fun ComposableLambdaCase(name: String = "") {
    var state by remember { mutableStateOf(0) }
    println(state)
    Button(
        onClick = {
            state++
        }
    ) {
        // TODO，为什么被设置为 invalidate，导致重组
        // rememberCmposableLambda update -> traceRead -> invalidate
        test()
        Text("Composable Lambda Case, $name")
    }
}

private fun test() {
    println("test")
}