package com.xdmrwu.recompose.spy.demo.case

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * @Author: wulinpeng
 * @Date: 2025/5/6 21:57
 * @Description:
 */

var intState by mutableStateOf(0)


//@Composable
//fun RecomposeTestCase() {
//    RecomposeTestUI(intState)
//}
//
//@Composable
//fun RecomposeTestUI(state: Int) {
//    println("")
//    Button(onClick = {
//        intState += 1
//    }) {
//        println("")
//        Text(text = "点击次数: $intState")
//    }
//}

@Composable
fun RecomposeTestCase() {
    RecomposeTestUI()
    RecomposeTestUI()
}

@Composable
fun RecomposeTestUI() {
    println("")
    Button(onClick = {
        intState += 1
    }) {
        println("")
        Text(text = "点击次数: $intState")
    }
}