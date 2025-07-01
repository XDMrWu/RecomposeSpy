package com.xdmrwu.recompose.spy.demo.case.compiler

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.util.trace

/**
 * @Author: wulinpeng
 * @Date: 2025/7/1 21:40
 * @Description:
 */
@Composable
fun TraceContent() = trace("Trace Content") {
    Text("")
}