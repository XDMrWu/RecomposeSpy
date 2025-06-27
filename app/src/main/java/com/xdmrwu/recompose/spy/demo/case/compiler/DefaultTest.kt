package com.xdmrwu.recompose.spy.demo.case.compiler

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

/**
 * @Author: wulinpeng
 * @Date: 2025/6/26 22:53
 * @Description:
 */
@Composable
fun DefaultTest(
    defaultComposableCall: Boolean = ComposableCall(),
    defaultPrimitive: Int = 1,
    defaultPrimitiveCall: Int = getInt(),
    defaultPrimitiveStableCall: Int = getStableInt(),
) {
    DefaultCalledTest(
        defaultComposableCall,
        defaultPrimitive,
        defaultPrimitiveCall,
        defaultPrimitiveStableCall
    )
}

@Composable
fun DefaultCalledTest(
    defaultComposableCall: Boolean,
    defaultPrimitive: Int,
    defaultPrimitiveCall: Int,
    defaultPrimitiveStableCall: Int,
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