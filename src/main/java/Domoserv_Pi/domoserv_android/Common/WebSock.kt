package Domoserv_Pi.domoserv_android.Common

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

        fun Update() {

        }

        fun disconnect() {
            webSock?.close(1000,null)
        }
    }
}

private class EchoWebSocketListener(private val password: String) : WebSocketListener() {
    private var m_crypto = CryptoFire()
    private var m_ready = false
    private val m_name = "Android"

    fun isReady(): Boolean {
        return m_ready
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        if (text.split(" ")?.count() == 50) {
            m_crypto = CryptoFire(50, 4, text)
            if (!m_crypto.Add_Encrypted_Key(m_name, password)) {
                webSocket.close(1000, null)
                println("Closing socket")
            } else {
                var data = "OK $m_name"
                webSocket.send(m_crypto.Encrypt_Data(data,m_name))
                m_ready = true
            }
        }
        else {
            println(m_crypto.Decrypt_Data(text, m_name))
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