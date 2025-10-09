package com.xdmrwu.recompose.spy.demo.case.compiler

import android.annotation.SuppressLint
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.flowOf

/**
 * @Author: wulinpeng
 * @Date: 2025/10/9 12:50
 * @Description:
 */

private var state1 by mutableStateOf(1)
private var state2 = mutableStateOf(1)

@SuppressLint("UnrememberedMutableState")
@Composable
fun StateChangeTest() {
    var state3 by mutableStateOf(1)
    var state4 = remember { mutableStateOf(1) }
    var state5 by getMutableState()
    var state6 = getMutableState()
    val state7 by flowOf(1).collectAsState(1)
    var state8 = flowOf(1).collectAsState(1)

    println(state1)
    println(state2.value)
    println(state3)
    println(state4.value)
    println(getState().value)
    println(state5)
    println(state6.value)
    println(state7)
    println(state8.value)

}

fun getState(): State<Int> {
    return state2
}

fun getMutableState(): MutableState<Int> {
    return state2
}

@Composable
private fun StateChange(count: Int) {
    println(count)
    println(state1)
    Text("StateChange")
}