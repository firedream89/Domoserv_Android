package Domoserv_Pi.domoserv_android

import Domoserv_Pi.domoserv_android.Common.NetworkError
import Domoserv_Pi.domoserv_android.Common.WebSock
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import okhttp3.Dispatcher
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.*
<<<<<<< HEAD
import okhttp3.WebSocket
=======
>>>>>>> feature/MainActivity


enum class State { Confort, Eco, HorsGel }
enum class Mode { Auto, SAuto, Manual }
enum class Zone { unused, Z1, Z2 }

class MainActivity : AppCompatActivity() {
    
    private var ws = WebSock()
    private val stateList = listOf("Confort", "Eco", "HorsGel")
    private val modeList = listOf("Auto", "SemiAuto", "Manual")

<<<<<<< HEAD
    inner class WebSocket() : WebSock() {
        override fun onMessage(webSocket: okhttp3.WebSocket, text: String) {
            super.onMessage(webSocket, text)
            updateField(mDecryptedText)
            mDecryptedText = ""
        }
    }
=======
    private var ws = WebSock()
    private val stateList = listOf("Confort", "Eco", "HorsGel")
    private val modeList = listOf("Auto", "SemiAuto", "Manual")
>>>>>>> feature/MainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent = Intent(this, ConnectionActivity::class.java)
        intent.putExtra("Path",this.filesDir.path)
        intent.putExtra("FirstAttempt",true)
        startActivityForResult(intent,0)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun startUpdate() {
        ws.send("CVOrder|GetZ1Order")
<<<<<<< HEAD
        ws.send("CVOrder|GetZ2Order")
        ws.send("CVOrder|GetZ1Status")
        ws.send("CVOrder|GetZ2Status")
        ws.send("CVOrder|GetRemainingTimeZ1")
        ws.send("CVOrder|GetRemainingTimeZ2")
        ws.send("CVOrder|GetTemp;0")
        ws.send("CVOrder|GetTemp;1")
        val date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val dateYear = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy")) + "-01-01"
        ws.send("CVOrder|GetDataCPTEnergy;$dateYear:$date")
=======
        while(!ws.isReadyRead());
        updateField(ws.getLastMessage())
        ws.send("CVOrder|GetZ2Order")
        while(!ws.isReadyRead());
        updateField(ws.getLastMessage())
        ws.send("CVOrder|GetZ1Status")
        while(!ws.isReadyRead());
        updateField(ws.getLastMessage())
        ws.send("CVOrder|GetZ2Status")
        while(!ws.isReadyRead());
        updateField(ws.getLastMessage())
        ws.send("CVOrder|GetRemainingTimeZ1")
        while(!ws.isReadyRead());
        updateField(ws.getLastMessage())
        ws.send("CVOrder|GetRemainingTimeZ2")
        while(!ws.isReadyRead());
        updateField(ws.getLastMessage())
        ws.send("CVOrder|GetTemp;0")
        while(!ws.isReadyRead());
        updateField(ws.getLastMessage())
        ws.send("CVOrder|GetTemp;1")
        while(!ws.isReadyRead());
        updateField(ws.getLastMessage())
        val date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val dateYear = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy")) + "-01-01"
        ws.send("CVOrder|GetDataCPTEnergy;$dateYear:$date")
        while(!ws.isReadyRead());
        updateField(ws.getLastMessage())
       // delay(60000)
        startUpdate()
>>>>>>> feature/MainActivity
    }

    fun updateField(data: String) {
        if(data.contains("CVOrder")) {
            if(data.contains("GetZ1Order")) {
                findViewById<TextView>(R.id.stateZ1).text = stateList[data.split("=").last().toInt()]
            }
            if(data.contains("GetZ2Order")) {
                findViewById<TextView>(R.id.stateZ2).text = stateList[data.split("=").last().toInt()]
            }
            if(data.contains("GetZ1Status")) {
                findViewById<TextView>(R.id.modeZ1).text = modeList[data.split("=").last().toInt()]
            }
            if(data.contains("GetZ2Status")) {
                findViewById<TextView>(R.id.modeZ2).text = modeList[data.split("=").last().toInt()]
            }
            if(data.contains("GetRemainingTimeZ1")) {
                val t = data.split("=").last().toInt()
                println(t)
                val h = t / 60 / 60
                val mn = t / 60 % 60
                val result = "${ when(h){
                        in 0..9 -> "0$h" 
                        else -> h
                    }}:${when(mn){
                        in 0..9 -> "0$mn" 
                        else -> mn
                    }}"
                findViewById<TextView>(R.id.timerZ1).text = result
            }
            if(data.contains("GetRemainingTimeZ2")) {
                val t = data.split("=").last().toInt()
                println(t)
                val h = t / 60 / 60
                val mn = t / 60 % 60
                val result = "${ when(h){
                    in 0..9 -> "0$h"
                    else -> h
                }}:${when(mn){
                    in 0..9 -> "0$mn"
                    else -> mn
                }}"
                findViewById<TextView>(R.id.timerZ2).text = result
            }
            if(data.contains("GetTemp;0")) {
                val temp = data.split("=").last().split(":")
                if(temp.count() == 3) {
                    val min = temp.first() + "°C"
                    val max = temp[1] + "°C"
                    val actual = temp.last() + "°C"
                    findViewById<TextView>(R.id.tempIntMin).text = min
                    findViewById<TextView>(R.id.tempIntMax).text = max
                    findViewById<TextView>(R.id.tempIntActual).text = actual
                }
            }
            if(data.contains("GetTemp;1")) {
                val temp = data.split("=").last().split(":")
                if(temp.count() == 3) {
                    val min = temp.first() + "°C"
                    val max = temp[1] + "°C"
                    val actual = temp.last() + "°C"
                    findViewById<TextView>(R.id.tempExtMin).text = min
                    findViewById<TextView>(R.id.tempExtMax).text = max
                    findViewById<TextView>(R.id.tempExtActual).text = actual
                }
            }
            if(data.contains("GetDataCPTEnergy")) {
                var all = data.split("=").last().split("\r").toMutableList()
                all.removeAt(all.count()-1)
                var daily = 0
                for (value in all) {
                    daily += value.split("|").last().toInt()
                }
                val dailyCons = "${daily/1000.toDouble()}kw/h"
                val dailyCost = "${daily/1000*0.1781}€"
                findViewById<TextView>(R.id.consDaily).text = dailyCons
                findViewById<TextView>(R.id.dailyCost).text = dailyCost
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == 0) {
            if (data != null && data.hasExtra("Ip") && data.hasExtra("Port") && data.hasExtra("Password")) {
                val ip = data.extras?.getString("Ip") ?: String()
                val port = data.extras?.getString("Port") ?: "0"
                val password = data.extras?.getString("Password") ?: String()

                ws.connect(ip, port.toInt(), password)

                while(ws.isOpen()) {
                    if(ws.isReady()) {
                        break
                    }
                }
                if(!ws.isReady()) {
                    val result = when (ws.getLastError()) {
                        NetworkError.PasswordError.ordinal -> getString(R.string.wsPasswordError)
                        NetworkError.DataError.ordinal -> getString(R.string.wsDataError)
                        else -> ws.getLastError().toString()
                    }
                    Toast.makeText(this,result,Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, ConnectionActivity::class.java)
                    intent.putExtra("Path",this.filesDir.path)
                    intent.putExtra("FirstAttempt",false)
                    startActivityForResult(intent,0)
                } else {
                    Toast.makeText(this,"Connected !",Toast.LENGTH_SHORT).show()
                    startUpdate()
                }
            }
        }
    }

    override fun finish() {
        ws.disconnect()
        super.finish()
    }
}
