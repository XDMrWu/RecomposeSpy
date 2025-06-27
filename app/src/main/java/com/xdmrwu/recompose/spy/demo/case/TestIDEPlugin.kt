package com.xdmrwu.recompose.spy.demo.case

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import java.io.OutputStreamWriter
import java.net.Socket
import kotlin.concurrent.thread

/**
 * @Author: wulinpeng
 * @Date: 2025/6/24 23:21
 * @Description:
 */
@Composable
fun TestIDEPlugin() {
    Button(onClick = {
        sendDataToPlugin("Hello from Compose Debug Tool!\nhahaha")
    }) {
        Text("Test IDE Plugin")
    }
}

private val PLUGIN_PORT = 50000 // 插件监听的端口，也是 ADB 转发的目标端口

private fun sendDataToPlugin(data: String) {
    // 在新线程中执行网络操作，避免阻塞 UI 线程
    thread {
        try {
            // 连接到本地回环地址和转发的端口
            // ADB 会将设备上的 127.0.0.1:50000 转发到宿主机上的 50000 端口
            val socket = Socket("127.0.0.1", PLUGIN_PORT)
            val writer = OutputStreamWriter(socket.getOutputStream(), Charsets.UTF_8)
            writer.write(data)
            writer.flush()
            socket.shutdownOutput() // 关闭输出流，表示数据发送完毕
//            socket.close()

//            runOnUiThread {
//                statusTextView.text = "数据已发送: $data"
//            }
            println("App: Data sent successfully: $data")
        } catch (e: Exception) {
//            runOnUiThread {
//                statusTextView.text = "发送数据失败: ${e.message}"
//            }
            e.printStackTrace()
            println("App: Failed to send data: ${e.message}")
        }
    }
}