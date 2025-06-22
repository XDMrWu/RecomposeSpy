package com.xdmrwu.recompose.spy.demo.case

import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

/**
 * @Author: wulinpeng
 * @Date: 2025/5/6 21:39
 * @Description:
 */

var count by mutableStateOf(0)

@Composable
fun RememberTestCase() {
    Text(text = "RememberTestCase $count", Modifier.clickable {
        count++
    })
}