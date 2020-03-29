package Domoserv_Pi.domoserv_android

import Domoserv_Pi.domoserv_android.Common.WebSock
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    @ExperimentalStdlibApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        println("Début du test")

        val ws = WebSock()
        ws.connect("192.168.1.73",52000, "0123456")


        println("Test Terminé")
    }
}
