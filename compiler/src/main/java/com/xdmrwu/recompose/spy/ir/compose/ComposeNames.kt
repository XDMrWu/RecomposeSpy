package com.xdmrwu.recompose.spy.ir.compose

import org.jetbrains.kotlin.name.Name

/**
 * copy from Compose Compiler
 */
object ComposeNames {
    val COMPOSER = Name.identifier("composer")
    val COMPOSER_PARAMETER = Name.identifier("\$composer")
    val CHANGED_PARAMETER = Name.identifier("\$changed")
    val FORCE_PARAMETER = Name.identifier("\$force")
    val STABILITY_FLAG = Name.identifier("\$stable")
    val STABILITY_PROP_FLAG = Name.identifier("\$stableprop")
    val STABILITY_GETTER_FLAG = "\$stableprop_getter"
    val DEFAULT_PARAMETER = Name.identifier("\$default")
    val JOINKEY = Name.identifier("joinKey")
    val STARTRESTARTGROUP = Name.identifier("startRestartGroup")
    val ENDRESTARTGROUP = Name.identifier("endRestartGroup")
    val UPDATE_SCOPE = Name.identifier("updateScope")
    val SOURCEINFORMATION = "sourceInformation"
    val SOURCEINFORMATIONMARKERSTART = "sourceInformationMarkerStart"
    val IS_TRACE_IN_PROGRESS = "isTraceInProgress"
    val TRACE_EVENT_START = "traceEventStart"
    val TRACE_EVENT_END = "traceEventEnd"
    val SOURCEINFORMATIONMARKEREND = "sourceInformationMarkerEnd"
    val UPDATE_CHANGED_FLAGS = "updateChangedFlags"
    val CURRENTMARKER = Name.identifier("currentMarker")
    val ENDTOMARKER = Name.identifier("endToMarker")
    val REMEMBER_COMPOSABLE_LAMBDA = "rememberComposableLambda"
    val REMEMBER_COMPOSABLE_LAMBDAN = "rememberComposableLambdaN"
    val DEFAULT_IMPLS = Name.identifier("ComposeDefaultImpls")
    val SHOULD_EXECUTE = Name.identifier("shouldExecute")
}
