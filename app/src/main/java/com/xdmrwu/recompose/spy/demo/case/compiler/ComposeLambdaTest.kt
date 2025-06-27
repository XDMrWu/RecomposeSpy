package com.xdmrwu.recompose.spy.demo.case.compiler

import androidx.compose.material3.Button
import androidx.compose.runtime.Composable

/**
 * @Author: wulinpeng
 * @Date: 2025/6/27 13:40
 * @Description:
 */
@Composable
fun TestComposeLambda() {
    ComposeWithLambda { text ->
        println("ComposeLambda: $text")
    }

    InlineComposeWithLambda { text ->
        println("ComposeLambda: $text")
    }
}

@Composable
fun ComposeWithLambda(
    ComposeLambda: @Composable (String) -> Unit
) {
    ComposeLambda("Hello, Compose!")
}

@Composable
inline fun InlineComposeWithLambda(
    ComposeLambda: @Composable (String) -> Unit
) {
    Button(onClick = {}) {
//        ComposeLambda()
    }
}