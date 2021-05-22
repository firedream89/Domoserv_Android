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


    fun connect(url: String, port: Int, password: String, keySize: Int, codeSize: Int, charFormat: Boolean) {
        mPassword = password
        val format = when(charFormat) {
            true -> CryptoFire.char.UTF16.ordinal
            false -> CryptoFire.char.UTF8.ordinal }
        mCrypto = CryptoFire(keySize,codeSize,format,"")
        mCrypto.Add_Encrypted_Key("client", mPassword,"")
        val ws: OkHttpClient? = OkHttpClient()
        val request = Request.Builder().url("ws://$url:$port").build()
        mWs = ws?.newWebSocket(request, this)
    }
    fun send(data: String) {
        when(mReady) {
            true -> mWs?.send(mCrypto.Encrypt_Data(data,"client"))
            false -> mWs?.send(data)
        }
    }

    fun disconnect() {
        mWs?.close(1000, null)
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
        Auth("")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        println("Crypted : $text")
        val result = Auth(text)

        if(mReady) {
            mDecryptedText = result
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

    enum class level {
        unhautorised, sendKey, sendHash, sendName, autorised
    }

    private var authLvl = 0

    fun Auth(data: String): String {
        if (authLvl == level.unhautorised.ordinal) {
            send(mCrypto.Get_Key())
            authLvl = level.sendKey.ordinal
        } else if (authLvl == level.sendKey.ordinal) {
            val result = mCrypto.Decrypt_Data(data, "client")
            println(mCrypto.Add_Encrypted_Key("server", mPassword, result))
            send(mCrypto.Key_To_SHA256("server"))
            authLvl = level.sendHash.ordinal
        } else if (authLvl == level.sendHash.ordinal) {
            if (data == "OK") {
                var result: String = mName
                result = mCrypto.Encrypt_Data(result, "client")
                send(result)
                authLvl = level.sendName.ordinal
            }
        } else if (authLvl == level.sendName.ordinal) {
            if (data == "READY") {
                authLvl = level.autorised.ordinal
                mReady = true
            }
        } else if (authLvl == level.autorised.ordinal) {
            return mCrypto.Decrypt_Data(data, "server")
        }
        return ""
    }
}