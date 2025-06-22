package com.xdmrwu.recompose.spy.demo.case

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * @Author: wulinpeng
 * @Date: 2025/6/22 18:07
 * @Description:
 */
val targetState = mutableStateOf(0)

@Composable
fun ComposeIrTest() {
    var rememberState by remember { mutableStateOf("") }

    val lifecycleOwner = LocalLifecycleOwner.current

    Text("${targetState.value} - $rememberState")
}