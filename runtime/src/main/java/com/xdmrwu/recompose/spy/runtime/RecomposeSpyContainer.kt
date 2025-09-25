@file:OptIn(ExperimentalComposeRuntimeApi::class)

package com.xdmrwu.recompose.spy.runtime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ExperimentalComposeRuntimeApi
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.NonSkippableComposable
import androidx.compose.runtime.RecomposeScope
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.tooling.CompositionObserver
import androidx.compose.runtime.tooling.observe

/**
 * @Author: wulinpeng
 * @Date: 2025/8/17 00:59
 * @Description:
 */
val LocalRecomposeSpy = staticCompositionLocalOf<RecomposeSpy?> { null }

@Composable
fun RecomposeSpyContainer(content: @Composable () -> Unit) {
    val recomposeSpy = remember {
        RecomposeSpy()
    }
    currentComposer.composition.observe(object: CompositionObserver {
        override fun onBeginComposition(
            composition: Composition,
            invalidationMap: Map<RecomposeScope, Set<Any>>
        ) {
            recomposeSpy.currentInvalidationMap = invalidationMap
        }

        override fun onEndComposition(composition: Composition) {

        }
    })
    CompositionLocalProvider(LocalRecomposeSpy provides recomposeSpy) {
        content()
    }
    DisposableEffect(Unit) {
        onDispose {
            recomposeSpy.dispose()
        }
    }
}

@Composable
@NonSkippableComposable
@NonRestartableComposable
fun StartRecomposeSpy(fqName: String, file: String, startLine: Int, endLine: Int, startOffset: Int, endOffset: Int,
                      hasDispatchReceiver: Boolean, hasExtensionReceiver: Boolean,
                      isLambda: Boolean, inline: Boolean,
                      hasReturnType: Boolean = false, nonSkippable: Boolean, nonRestartable: Boolean) {
    LocalRecomposeSpy.current?.StartRecomposeSpy(
        fqName, file, startLine, endLine, startOffset, endOffset,
        hasDispatchReceiver, hasExtensionReceiver, isLambda, inline,
        hasReturnType, nonSkippable, nonRestartable
    )

}

@Composable
@NonSkippableComposable
@NonRestartableComposable
fun EndRecomposeSpy(
    fqName: String,
    dirties: Array<Int>,
    paramNames: Array<String>,
    unusedParams: Array<String>,
    defaultBitMasks: Array<Int>,
) {
    LocalRecomposeSpy.current?.EndRecomposeSpy(
        fqName, dirties, paramNames, unusedParams, defaultBitMasks
    )
}