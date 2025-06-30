package com.xdmrwu.recompose.spy.demo.case.compiler

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * @Author: wulinpeng
 * @Date: 2025/6/26 22:53
 * @Description:
 */
@Composable
fun DefaultTestCase() {
    var state by remember { mutableStateOf(0) }
    DefaultTest(changedParam = System.currentTimeMillis())
    println(state)
    Button(onClick = {
        state++
    }) {
        Text("Click to recompose: $state")
    }
}

@Composable
fun DefaultTest(
    defaultComposableCall: Boolean = ComposableCall(),
    changedParam: Long,
    defaultPrimitive: Int = 1,
    defaultPrimitiveCall: Int = getInt(),
    defaultPrimitiveStableCall: Int = getStableInt(),
    defaultObject: Any = getObject(),
    timestamp: Long = System.currentTimeMillis(),
) {
    println("$changedParam")
    DefaultCalledTest(
        defaultComposableCall,
        defaultPrimitive,
        defaultPrimitiveCall,
        defaultPrimitiveStableCall,
        defaultObject,
        timestamp
    )
}

@Composable
fun DefaultCalledTest(
    defaultComposableCall: Boolean,
    defaultPrimitive: Int,
    defaultPrimitiveCall: Int,
    defaultPrimitiveStableCall: Int,
    defaultObject: Any,
    timestamp: Long
) {
    println("DefaultTest called with: " +
            "defaultComposableCall=$defaultComposableCall, " +
            "defaultPrimitive=$defaultPrimitive, " +
            "defaultPrimitiveCall=$defaultPrimitiveCall, " +
            "defaultPrimitiveStableCall=$defaultPrimitiveStableCall")
}


@Composable
fun ComposableCall(): Boolean {
    return true
}

fun getInt(): Int {
    return 1
}

@Stable
fun getStableInt(): Int {
    return 1
}

fun getObject(): Any {
    return Any()
}

@Composable
fun LongDefaultParam(
    param1: Long = System.currentTimeMillis(),
    param2: Long = System.currentTimeMillis(),
    param3: Long = System.currentTimeMillis(),
    param4: Long = System.currentTimeMillis(),
    param5: Long = System.currentTimeMillis(),
    param6: Long = System.currentTimeMillis(),
    param7: Long = System.currentTimeMillis(),
    param8: Long = System.currentTimeMillis(),
    param9: Long = System.currentTimeMillis(),
    param10: Long = System.currentTimeMillis(),
    param11: Long = System.currentTimeMillis(),
    param12: Long = System.currentTimeMillis(),
    param13: Long = System.currentTimeMillis(),
    param14: Long = System.currentTimeMillis(),
    param15: Long = System.currentTimeMillis(),
    param16: Long = System.currentTimeMillis(),
    param17: Long = System.currentTimeMillis(),
    param18: Long = System.currentTimeMillis(),
    param19: Long = System.currentTimeMillis(),
    param20: Long = System.currentTimeMillis(),
    param21: Long = System.currentTimeMillis(),
    param22: Long = System.currentTimeMillis(),
    param23: Long = System.currentTimeMillis(),
    param24: Long = System.currentTimeMillis(),
    param25: Long = System.currentTimeMillis(),
    param26: Long = System.currentTimeMillis(),
    param27: Long = System.currentTimeMillis(),
    param28: Long = System.currentTimeMillis(),
    param29: Long = System.currentTimeMillis(),
    param30: Long = System.currentTimeMillis(),
    param31: Long = System.currentTimeMillis(),
    param32: Long = System.currentTimeMillis(),
) {
    println("LongDefaultParam called with: " +
            "param1=$param1, param2=$param2, param3=$param3, param4=$param4, " +
            "param5=$param5, param6=$param6, param7=$param7, param8=$param8, " +
            "param9=$param9, param10=$param10, param11=$param11, param12=$param12, " +
            "param13=$param13, param14=$param14, param15=$param15, param16=$param16, " +
            "param17=$param17, param18=$param18, param19=$param19, param20=$param20, " +
            "param21=$param21, param22=$param22, param23=$param23, param24=$param24, " +
            "param25=$param25, param26=$param26, param27=$param27, param28=$param28, " +
            "param29=$param29, param30=$param30, param31=$param31, param32=$param32")
}