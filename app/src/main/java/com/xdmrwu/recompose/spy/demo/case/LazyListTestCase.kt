package com.xdmrwu.recompose.spy.demo.case

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter

/**
 * @Author: wulinpeng
 * @Date: 2025/7/1 22:43
 * @Description:
 */
@Composable
fun LazyListTestCase() {
    LazyColumn {
        item {
            Text("first item")
        }
        item {
            Button(onClick = { }) {
                Text("Second item")
            }
        }
        item {
            Icon(painter = ColorPainter(Color.Red), "")
        }
    }
}