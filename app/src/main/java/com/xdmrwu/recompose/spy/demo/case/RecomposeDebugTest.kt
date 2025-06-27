package com.xdmrwu.recompose.spy.demo.case

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * @Author: wulinpeng
 * @Date: 2025/6/12 23:08
 * @Description:
 */

val testState = mutableStateOf(0)

@Composable
fun RecomposeDebugTest() {
    Log.d("RecomposeDebugTest", RuntimeException().stackTraceToString())

//    TrackStateChanges(mapOf(
//        "state" to state
//    ))

    Column {
        // TODO 这种不会识别到 lambda 被 inline，RecomposeSpy 分析有问题
        Button(onClick = {
            testState.value += 1
        }) {
            Text("Click")
        }
        RecomposeUI(testState.value)
    }

    ComposeLambdaTest {
        Text(it)
    }

}


@Composable
private fun RecomposeUI(count: Int) {
    Text(
        text = "State: $count",
        modifier = Modifier,
        color = Color.Black
    )
}

private var countState by mutableStateOf(0)

@Composable
private fun CountTest(state: Int) {
    Text(
        text = "State: $state, $countState",
        modifier = Modifier,
        color = Color.Black
    )
}


@Composable
private fun ParamTest(p1: Int, p2: Int, p3: Int, p4: Int, p5: Int, p6: Int, p7: Int, p8: Int, p9: Int, p10: Int, p11: Int, p12: Int, p13: Int, p14: Int, p15: Int) {
    Text(
        text = "State: $testState, $p1, $p2, $p3, $p4, $p10, $p11, $p12, $p13, $p14, $p15",
        modifier = Modifier,
        color = Color.Black
    )
}

@Composable
fun ComposeLambdaTest(content: @Composable (name: String) -> Unit) {
    content("")
    Text(
        text = "Lambda Test",
        modifier = Modifier,
        color = Color.Black
    )
}