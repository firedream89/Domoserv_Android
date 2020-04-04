package Domoserv_Pi.domoserv_android

import Domoserv_Pi.domoserv_android.Common.WebSock
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


enum class State { Confort, Eco, HorsGel }
enum class Mode { Auto, SAuto, Manual }
enum class Zone { unused, Z1, Z2 }

class MainActivity : AppCompatActivity() {

    var ws = WebSock()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent = Intent(this, ConnectionActivity::class.java)
        startActivityForResult(intent,0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == 0) {
            if (data != null) {
                if (data.hasExtra("Ip") && data.hasExtra("Port") && data.hasExtra("Password")) {
                    ws.connect(data.extras?.getString("Ip")!!,data.extras?.getString("Port")!!.toInt(),data.extras?.getString("Password")!!)
                    while(ws.isOpen()) {
                        if(ws.isReady()) {
                            break
                        }
                    }
                    if(!ws.isReady()) {
                        Toast.makeText(this,"Fail !",Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, ConnectionActivity::class.java)
                        startActivityForResult(intent,0)
                    }
                    else {
                        Toast.makeText(this,"Connected !",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
