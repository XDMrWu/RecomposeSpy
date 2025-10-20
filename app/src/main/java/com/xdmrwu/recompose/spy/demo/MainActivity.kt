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
import androidx.compose.runtime.DontMemoize
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
import com.xdmrwu.recompose.spy.demo.case.analyze.ComposableLambdaCase
import com.xdmrwu.recompose.spy.demo.case.analyze.InlineRecomposeTest
import com.xdmrwu.recompose.spy.demo.case.analyze.ParamChangeTest
import com.xdmrwu.recompose.spy.demo.case.analyze.SubRecomposeTest
import com.xdmrwu.recompose.spy.demo.case.compiler.StateChangeTest
import com.xdmrwu.recompose.spy.demo.theme.RecomposeSpyTheme
import com.xdmrwu.recompose.spy.runtime.RecomposeSpy
import com.xdmrwu.recompose.spy.runtime.RecomposeSpyContainer
import com.xdmrwu.recompose.spy.runtime.RecomposeSpyTrackNode
import com.xdmrwu.recompose.spy.runtime.RecomposeSpyTrackScope
import com.xdmrwu.recompose.spy.runtime.printer.IRecomposeSpyReporter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        RecomposeSpy.init(RecomposeSpyTrackScope.SCOPE_PROJECT_SOURCE_WITH_STACK_TRACE)
        RecomposeSpy.registerReporter(object : IRecomposeSpyReporter {
            override fun onRecompose(node: RecomposeSpyTrackNode) {
                println("[MainActivity] onRecompose $node")
            }

        })
        setContent {
            RecomposeSpyContainer {
                RecomposeSpyTheme {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        TestCaseList(
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

data class TestCase(
    val name: String,
    val testCase: @Composable () -> Unit
)

val testCases = listOf(
    TestCase("Inline Recompose Test") {
        InlineRecomposeTest()
    },
    TestCase("Sub Recompose Test") {
        SubRecomposeTest()
    },
    TestCase("Param Change Test") {
        ParamChangeTest()
    },
    TestCase("Composable Lambda Test") {
        ComposableLambdaCase()
    },
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
                        test()
                        // TODO，为什么被设置为 invalidate，导致重组
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

private fun test() {
    println("test")
}