package Domoserv_Pi.domoserv_android.Common

import okhttp3.*

open class WebSock {
    private var ws: OkHttpClient? = OkHttpClient()

    fun connect(url: String, port: Int, password: String) {
        val request = Request.Builder().url("ws://$url:$port").build()
        val listener = EchoWebSocketListener(password)
        val webSock = ws?.newWebSocket(request, listener)
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
    private var crypto = CryptoFire()
    private var ready = false
    private val name = "Android"

    fun isReady(): Boolean {
        return ready
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        if (text.split(" ")?.count() == 50) {
            crypto = CryptoFire(50, 4, text)
            if (!crypto.Add_Encrypted_Key(name, password)) {
                webSocket.close(1000, null)
                println("Closing socket")
            } else {
                var data = "OK $name"
                webSocket.send(crypto.Encrypt_Data(data,name))
                ready = true
            }
        }
        else {
            println(crypto.Decrypt_Data(text, name))
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