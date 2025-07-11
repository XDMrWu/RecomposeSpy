package com.xdmrwu.recompose.spy.runtime.printer

import android.util.Log
import com.xdmrwu.recompose.spy.runtime.RecomposeSpyTrackNode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.PrintWriter
import java.net.Socket
import kotlin.coroutines.CoroutineContext


/**
 * @Author: wulinpeng
 * @Date: 2025/7/3 21:28
 * @Description:
 */
class IdePluginReporter: IRecomposeSpyReporter, CoroutineScope {

    companion object {
        const val IDE_HOST = "127.0.0.1"
        const val IDE_PORT = 50000
    }

    override val coroutineContext: CoroutineContext = Dispatchers.IO + SupervisorJob()

    private val taskFlow = MutableSharedFlow<suspend () -> Unit>(extraBufferCapacity = Int.MAX_VALUE)

    private val json = Json { ignoreUnknownKeys = true }

    init {
        launch {
            taskFlow.collect {
                runCatching {
                    it.invoke()
                }.onFailure { e ->
                    Log.e("IdePluginPrinter", "Error executing task: ${e.message}", e)
                }
            }
        }
    }

    override fun onRecompose(node: RecomposeSpyTrackNode) {
        sendToIde(json.encodeToString<RecomposeSpyTrackNode>(RecomposeSpyTrackNode.serializer(), node))
    }

    private fun sendToIde(message: String) {
        taskFlow.tryEmit {
            var socket: Socket? = null
            runCatching {
                socket = Socket(IDE_HOST, IDE_PORT)
                // 获取输出流并发送消息
                val out = PrintWriter(socket.getOutputStream(), true)
                out.println(message)
                out.flush()
                delay(50)
                out.close()
            }
            socket?.close()
        }
    }
}