package Domoserv_Pi.domoserv_android.Common

import Domoserv_Pi.domoserv_android.exceptions.CryptoFireException
import okhttp3.*

open class WebSock {
    private var m_ws: OkHttpClient? = OkHttpClient()

    fun connect(url: String, port: Int, password: String) {
        val request = Request.Builder().url("ws://$url:$port").build()
        val listener = EchoWebSocketListener(password)
        val webSock = m_ws?.newWebSocket(request, listener)
        if (webSock != null) {
            while(!listener.isReady()) {}
            println("Connected")
        }

        fun send(data: String) {
            webSock?.send(data)
        }

        fun update() {

        }

        fun disconnect() {
            webSock?.close(1000, null)
        }
    }
}

private class EchoWebSocketListener(private val password: String) : WebSocketListener() {
    private lateinit var m_crypto: CryptoFire
    private var m_ready = false
    private val m_name = "Android"

    fun isReady(): Boolean {
        return m_ready
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
    }

    @ExperimentalUnsignedTypes
    override fun onMessage(webSocket: WebSocket, text: String) {
        m_crypto = CryptoFire(text)
        try {
            if (text.split(" ").count() == 50) {
                m_crypto.addEncryptedKey(m_name, password)
                var data = "OK $m_name"
                webSocket.send(m_crypto.encryptData(data, m_name))
                m_ready = true
            } else {
                println(m_crypto.decryptData(text, m_name))
            }
            //TODO Split CryptoFireExceptions into several exceptions (Encrypt / Decrypt)
        } catch (e: CryptoFireException) {
            webSocket.close(1000, null)
            println("Closing socket")
            //TODO Error message to display to inform users

        }
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket.close(1000, null)
        println("Closing : $code / $reason")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        println("Error : " + t.message)
    }
}