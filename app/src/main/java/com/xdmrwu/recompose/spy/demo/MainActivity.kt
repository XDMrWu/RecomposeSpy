@file:OptIn(InternalComposeTracingApi::class, ExperimentalComposeRuntimeApi::class)

package com.xdmrwu.recompose.spy.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composer
import androidx.compose.runtime.ExperimentalComposeRuntimeApi
import androidx.compose.runtime.InternalComposeTracingApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.xdmrwu.recompose.spy.demo.case.RecomposeDebugTest
import com.xdmrwu.recompose.spy.demo.case.compiler.NonRestartableTest
import com.xdmrwu.recompose.spy.demo.case.RememberTestCase
import com.xdmrwu.recompose.spy.demo.case.TestIDEPlugin
import com.xdmrwu.recompose.spy.demo.case.compiler.NonSkippableTest
import com.xdmrwu.recompose.spy.demo.theme.ComposeDebugToolTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeDebugToolTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TestCaseList(
                        modifier = Modifier.padding(innerPadding)
                    )
                    TestReturnV1(false)
                    TestReturnV1(true)
                    TestReturnV2(false)
                    TestReturnV2(true)
                }
            }
        }
    }
}
@Composable
fun TestReturnV1(a: Boolean): Int {
    var count = getCount()
    if (a) {
        return count
    }
    return count + 1
}

@Composable
fun TestReturnV2(a: Boolean) {
    var count = getCount()
    Empty()
    if (a) {
        Empty()
        return
    }
    Empty()
    return
}

@Composable
fun Empty() {

}

fun getCount(): Int {
    return 42
}

data class TestCase(
    val name: String,
    val testCase: @Composable () -> Unit
)

val testCases = listOf(
    TestCase("Remember") {
        RememberTestCase()
    },
    TestCase("NonRestartableTest") {
        NonRestartableTest()
    },
    TestCase("NonSkippableTest") {
        NonSkippableTest("")
    },
    TestCase("Recompose Debug Test") {
        RecomposeDebugTest()
    },
    TestCase("Test IDE Plugin") {
         TestIDEPlugin()
    }
)

@Composable
fun TestCaseList(modifier: Modifier = Modifier) {

    var currentTestCase by remember { mutableStateOf<TestCase?>(null) }

    Box(modifier.fillMaxSize()) {
        Column(
            Modifier
                .scrollable(rememberScrollableState {it}, Orientation.Vertical)
                .fillMaxSize()
        ) {
            testCases.forEach {
                key(it.name) {
                    Button(
                        onClick = {
                            currentTestCase = it
                        }
                    ) {
                        Text(it.name, modifier = Modifier.padding(8.dp), color = Color.Black)
                    }
                }
            }
        }
        if (currentTestCase != null) {
            Box(Modifier.fillMaxSize().background(Color.White)) {
                currentTestCase?.testCase()
            }
        }
    }

    BackHandler(currentTestCase != null) {
        currentTestCase = null
    }
}