package Domoserv_Pi.domoserv_android.Common

import okhttp3.*

enum class NetworkError { NoError, PasswordError, DataError }

open class WebSock : WebSocketListener() {
    private var mWs: WebSocket? = null
    private var mCrypto = CryptoFire()
    private var mReady = false
    private var mOpen = true
    private val mName = "Android"
    private var mPassword = ""
    var mDecryptedText = ""
    var mError = NetworkError.NoError.ordinal

    fun connect(url: String, port: Int, password: String) {
        mPassword = password
        val ws: OkHttpClient? = OkHttpClient()
        val request = Request.Builder().url("ws://$url:$port").build()
        mWs = ws?.newWebSocket(request, this)
    }
    fun send(data: String) {
        mWs?.send(mCrypto.Encrypt_Data(data,"Android"))
    }

    fun disconnect() {
        mWs?.close(1000,null)
    }

    fun isReady(): Boolean {
        return mReady
    }

    fun isOpen(): Boolean {
        return mOpen
    }

    fun getLastError(): Int {
        return mError
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        mOpen = true
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        if (text.split(" ").count() == 50) {
            mCrypto = CryptoFire(50, 4, text)
            if (!mCrypto.Add_Encrypted_Key(mName, mPassword)) {
                webSocket.close(1000, "Bad encrypted key")
                println("Closing socket")
                mReady = false
                mOpen = false
            } else {
                var data = "${NetworkError.NoError.ordinal} $mName"
                webSocket.send(mCrypto.Encrypt_Data(data,mName))
            }
        }
        else {
            when (text) {
                NetworkError.NoError.ordinal.toString() -> mReady = true
                NetworkError.PasswordError.ordinal.toString() -> {
                    println("Password Error")
                    mReady = false
                }
                NetworkError.DataError.ordinal.toString() -> {
                    println("Data Error")
                    mReady = false
                }
                else -> mDecryptedText = mCrypto.Decrypt_Data(text, mName)
            }
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