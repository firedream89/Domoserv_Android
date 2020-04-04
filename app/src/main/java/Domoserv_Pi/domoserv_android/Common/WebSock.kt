package Domoserv_Pi.domoserv_android.Common

import okhttp3.*

open class WebSock {
    private var mWs: WebSocket? = null
    private var mWsListener: EchoWebSocketListener? = null

    fun connect(url: String, port: Int, password: String) {
        val ws: OkHttpClient? = OkHttpClient()
        val request = Request.Builder().url("ws://$url:$port").build()
        mWsListener = EchoWebSocketListener(password)
        mWs = ws?.newWebSocket(request, mWsListener!!)
    }
    fun send(data: String) {
        mWs?.send(data)
    }

    fun Update() {

    }

    fun disconnect() {
        mWs?.close(1000,null)
    }

    fun isReady(): Boolean {
        if(mWsListener != null) {
            return mWsListener!!.isReady()
        }
        return false
    }
    fun isOpen(): Boolean {
        if(mWsListener != null) {
            return mWsListener!!.isOpen()
        }
        return false
    }
}

private class EchoWebSocketListener(private val password: String) : WebSocketListener() {
    private var mCrypto = CryptoFire()
    private var mReady = false
    private var mOpen = true
    private val mName = "Android"

    fun isReady(): Boolean {
        return mReady
    }

    fun isOpen(): Boolean {
        return mOpen
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        mOpen = true
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        if (text.split(" ").count() == 50) {
            mCrypto = CryptoFire(50, 4, text)
            if (!mCrypto.Add_Encrypted_Key(mName, password)) {
                webSocket.close(1000, "Bad encrypted key")
                println("Closing socket")
                mReady = false
                mOpen = false
            } else {
                var data = "OK $mName"
                webSocket.send(mCrypto.Encrypt_Data(data,mName))
                mReady = true
            }
        }
        else {
            println(mCrypto.Decrypt_Data(text, mName))
        }
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket.close(1000, null)
        println("Closing : $code / $reason")
        mReady = false
        mOpen = false
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        println("Error : " + t.message)
        mReady = false
        mOpen = false
    }
}