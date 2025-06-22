package com.xdmrwu.recompose.spy.demo.case.compiler

import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

/**
 * @Author: wulinpeng
 * @Date: 2025/5/11 21:57
 * @Description:
 * @NonRestartableComposable 不支持重启，不会走startRestartGroup/endRestartGroup，会导致重组范围被扩大至父compose
 *
 */
@Composable
fun NonRestartableTest() {
    println("[ComposeDebugTool] NonRestartableTest called")
    StateAwareCompose()
}


@Composable
@NonRestartableComposable
private fun StateAwareCompose() {
    println("[ComposeDebugTool] StateAwareCompose called")
    var state by remember { mutableStateOf(1) }
    Text("StateAwareCompose $state", Modifier.clickable {
        state++
    })
}