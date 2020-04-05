package Domoserv_Pi.domoserv_android

import Domoserv_Pi.domoserv_android.Common.WebSock
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File


enum class State { Confort, Eco, HorsGel }
enum class Mode { Auto, SAuto, Manual }
enum class Zone { unused, Z1, Z2 }

class MainActivity : AppCompatActivity() {

    var ws = WebSock()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent = Intent(this, ConnectionActivity::class.java)
        intent.putExtra("Path",this.filesDir.path)
        intent.putExtra("FirstAttempt",true)
        startActivityForResult(intent,0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == 0) {
            if (data != null && data.hasExtra("Ip") && data.hasExtra("Port") && data.hasExtra("Password")) {
                val ip = data.extras?.getString("Ip") ?: String()
                val port = data.extras?.getString("Port") ?: "0"
                val password = data.extras?.getString("Password") ?: String()

                ws.connect(ip,port.toInt(),password)
                while(ws.isOpen()) {
                    if(ws.isReady()) {
                        break
                    }
                }
                if(!ws.isReady()) {
                    Toast.makeText(this,"Fail !",Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, ConnectionActivity::class.java)
                    intent.putExtra("Path",this.filesDir.path)
                    intent.putExtra("FirstAttempt",false)
                    startActivityForResult(intent,0)
                } else {
                    Toast.makeText(this,"Connected !",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
