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

    var name: String = "AAA"

    // composableLambdaInstance, text
    ComposeWithLambda(ComposeLambda1 = {
        println("ComposeLambda: $it")
    })

    // rememberComposableLambda，text；
    ComposeWithLambda { text ->
        println("ComposeLambda: $text $name")
    }

    // 普通 lambda, replacegroup; no skip; no restart
    ComposeWithLambdaReturn { text ->
        "ComposeLambda: $text"
    }

    // 普通 lambda; no skip; no restart
    ComposeWithLambdaReturn { text ->
        "ComposeLambda: $text $name"
    }

    // 普通 lambda; no skip; no restart
    InlineComposeWithLambda { text ->
        println("ComposeLambda: $text")
    }

    // 普通 lambda; no skip; no restart
    InlineComposeWithLambda {
        println("ComposeLambda: $it $name")
    }

    // 和普通compose lambda方法一样
    InlineComposeWithNoinline {
        println("ComposeLambda: $it")
    }

    // 和普通compose lambda方法一样
    InlineComposeWithNoinline {
        println("ComposeLambda: $it $name")
    }

    // 和 inline or return 一样
    InlineComposeWithLambdaReturn {
        "ComposeLambda: $it"
    }

    // 和 inline or return 一样
    InlineComposeWithLambdaReturn {
        "ComposeLambda: $it $name"
    }
}

@Composable
fun ComposeWithLambda(
    ComposeLambda1: @Composable (String) -> Unit
) {
    ComposeLambda1("Hello, Compose!")
}

@Composable
fun ComposeWithLambdaReturn(
    ComposeLambda: @Composable (String) -> String
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

@Composable
inline fun InlineComposeWithNoinline(
    noinline ComposeLambda: @Composable (String) -> Unit
) {
    Button(onClick = {}) {
        ComposeLambda("")
    }
}

@Composable
inline fun InlineComposeWithLambdaReturn(
    ComposeLambda: @Composable (String) -> String
) {
    Button(onClick = {}) {
//        ComposeLambda()
    }
}