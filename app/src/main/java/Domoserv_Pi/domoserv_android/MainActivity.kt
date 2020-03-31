package Domoserv_Pi.domoserv_android

import Domoserv_Pi.domoserv_android.Common.WebSock
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

enum class State { Confort, Eco, HorsGel }
enum class Mode { Auto, SAuto, Manual }
enum class Zone { unused, Z1, Z2 }

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val ws = WebSock()
        ws.connect("192.168.1.73",52000, "0123456")

    }
}
