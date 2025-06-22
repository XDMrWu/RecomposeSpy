package com.xdmrwu.recompose.spy.demo.case.compiler

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.NonSkippableComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * @Author: wulinpeng
 * @Date: 2025/5/11 21:57
 * @Description:
 * @NonSkippableComposable 用于判断当前 Composable 整体是否可以跳过
 * 看起来只有用到了参数，才会去对比参数和上一次的 value，否则直接使用传入的$changed判断
 * 每个参数用 4 位，一个 int 的 changed 可以表示 8 个参数，超过怎么办?
 * 每个参数用 3 位，一个 int 的 changed 可以表示 10 个参数，超过怎么办?
 * 每个用到的参数都会用$composer.changed(param2)判断，但是可能某一个参数不做判断，那 changed 方法如何确定当前传递的参数第几个
 * $changed 和 dirty 的关系是什么，为什么要有两个bitmask 来判断
 */

class Foo(val a : String)

class FooUnstable(var a : String)

@Composable
@NonSkippableComposable
//@ExplicitGroupsComposable
//@NonRestartableComposable
fun NonSkippableTest(value: String) {
    var state by remember { mutableStateOf(1) }
    println("[ComposeDebugTool] NonRestartableTest called")
    Button(onClick = {
        state++
    }) {
        Text("NonSkippableTest")
    }
    // 读取一下 state
    println(state)
    SubComposableV1()
    SubComposableV2("1")
    SubComposableV3(value)
    SubComposableV5(10.dp)
    SubComposableV6(listOf(""))
    SubComposableV6(listOf(Foo("")))
    SubComposableV7(Foo(""))
}


@Composable
private fun SubComposableV1() {
    println("[ComposeDebugTool] SubComposableV2 called")
}

@Composable
private fun SubComposableV2(value: String) {
    println("[ComposeDebugTool] SubComposableV2 called")
}

@Composable
private fun SubComposableV3(value: String) {
    println("[ComposeDebugTool] SubComposableV3 called, value: $value")
}


@Composable
private fun SubComposableV4(param1: String, param2: Int, param3: Boolean) {
    println("[ComposeDebugTool] SubComposableV4 called, param1: $param1, param2: $param2, param3: $param3")
}

@Composable
private fun SubComposableV5(param: Dp) {
    println("[ComposeDebugTool] SubComposableV4 called, param: $param")
}

@Composable
private fun <T> SubComposableV6(param: List<T>) {
    println("[ComposeDebugTool] SubComposableV4 called, param: $param")
}

@Composable
private fun <T> SubComposableV7(param: T) {
    println("[ComposeDebugTool] SubComposableV4 called, param: $param")
}

@Composable
@NonRestartableComposable
fun UnRestartableComposableWithUnStableParam(stableParam: Int, unstableParam: FooUnstable) {
    used(stableParam)
    used(unstableParam)
}


private fun used(param: Any) {}