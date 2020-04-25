package Domoserv_Pi.domoserv_android

import Domoserv_Pi.domoserv_android.Common.NetworkError
import Domoserv_Pi.domoserv_android.Common.WebSock
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalDate
import java.time.format.DateTimeFormatter


enum class State { Confort, Eco, HorsGel }
enum class Mode { Auto, SAuto, Manual }
enum class Zone { unused, Z1, Z2 }

class MainActivity : AppCompatActivity() {

    private var ws = WebSocket()

    private val stateList = listOf("Confort", "Eco", "HorsGel")
    private val modeList = listOf("Auto", "SemiAuto", "Manual")

    inner class WebSocket() : WebSock() {
        override fun onMessage(webSocket: okhttp3.WebSocket, text: String) {
            super.onMessage(webSocket, text)
            updateField(mDecryptedText)
            mDecryptedText = ""
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent = Intent(this, ConnectionActivity::class.java)

        intent.putExtra("Path",this.filesDir.path)
        intent.putExtra("FirstAttempt",true)
        startActivityForResult(intent,0)

        findViewById<Button>(R.id.updateButton).setOnClickListener {
            setUpdateCvOrder()
        }

        findViewById<TextView>(R.id.stateZ1).setOnClickListener {
            showDialog("Zone 1 : State",0)
        }
        findViewById<TextView>(R.id.stateZ2).setOnClickListener {
            showDialog("Zone 2 : State",0)
        }
        findViewById<TextView>(R.id.modeZ1).setOnClickListener {
            showDialog("Zone 1 : Mode",1)
        }
        findViewById<TextView>(R.id.modeZ2).setOnClickListener {
            showDialog("Zone 2 : Mode",1)
        }
    }

    private fun setUpdateCvOrder() {
        var clickable = false
        val stateZ1 = findViewById<TextView>(R.id.stateZ1)
        clickable = when(stateZ1.isClickable) {
            true -> false
            false -> true
        }
        val color = when(clickable) {
            true -> Color.BLUE
            false -> Color.BLACK
        }
        stateZ1.isClickable = clickable
        findViewById<TextView>(R.id.stateZ2).isClickable = clickable
        findViewById<TextView>(R.id.modeZ1).isClickable = clickable
        findViewById<TextView>(R.id.modeZ2).isClickable = clickable
        stateZ1.setTextColor(color)
        findViewById<TextView>(R.id.stateZ2).setTextColor(color)
        findViewById<TextView>(R.id.modeZ1).setTextColor(color)
        findViewById<TextView>(R.id.modeZ2).setTextColor(color)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showDialog(title: String, type: Int) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.set_state_mode_dialog)
        val body = dialog.findViewById(R.id.title) as TextView
        body.text = title

        val zone = when(title.contains("1")) {
            true -> Zone.Z1.ordinal
            false -> Zone.Z2.ordinal
        }

        val bt1 = dialog.findViewById(R.id.bt1) as Button
        val bt2 = dialog.findViewById(R.id.bt2) as Button
        val bt3 = dialog.findViewById(R.id.bt3) as Button

        if(type == 1) {
            bt1.text = "Manual"
            bt2.text = "Automatic"
            bt3.visibility = View.INVISIBLE
        }
        bt1.setOnClickListener {
            when(type) {
                0 -> changeState(zone, State.Confort.ordinal)
                1 -> changeMode(zone, Mode.Manual.ordinal)
            }
            dialog.dismiss()
        }
        bt2.setOnClickListener {
            when(type) {
                0 -> changeState(zone, State.Eco.ordinal)
                1 -> changeMode(zone, Mode.Auto.ordinal)
            }
            dialog.dismiss()
        }
        bt3.setOnClickListener {
            when(type) {
                0 -> changeState(zone, State.HorsGel.ordinal)
                1 -> changeMode(zone, Mode.SAuto.ordinal)
            }
            dialog.dismiss()
        }
        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun changeState(zone: Int, state: Int) {
        ws.send("CVOrder|SetZ${zone}Order=$state")
        startUpdate()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun changeMode(zone: Int, mode: Int) {
        ws.send("CVOrder|SetZ${zone}Status=$mode")
        startUpdate()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun startUpdate() {
        ws.send("CVOrder|GetZ1Order")
        ws.send("CVOrder|GetZ2Order")
        ws.send("CVOrder|GetZ1Status")
        ws.send("CVOrder|GetZ2Status")
        ws.send("CVOrder|GetRemainingTimeZ1")
        ws.send("CVOrder|GetRemainingTimeZ2")
        ws.send("CVOrder|GetTemp;0")
        ws.send("CVOrder|GetTemp;1")
        val date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val dateYear = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy")) + "-01-01"
        ws.send("CVOrder|GetDataCPTEnergy;$date:$date")
    }

    fun updateField(data: String) {
        if (data.contains("CVOrder")) {
            if (data.contains("GetZ1Order")) {
                findViewById<TextView>(R.id.stateZ1).text =
                    stateList[data.split("=").last().toInt()]
            }
            if (data.contains("GetZ2Order")) {
                findViewById<TextView>(R.id.stateZ2).text =
                    stateList[data.split("=").last().toInt()]
            }
            if (data.contains("GetZ1Status")) {
                findViewById<TextView>(R.id.modeZ1).text = modeList[data.split("=").last().toInt()]
            }
            if (data.contains("GetZ2Status")) {
                findViewById<TextView>(R.id.modeZ2).text = modeList[data.split("=").last().toInt()]
            }
            if (data.contains("GetRemainingTimeZ1")) {
                val t = data.split("=").last().toInt()
                println(t)
                val h = t / 60 / 60
                val mn = t / 60 % 60
                val result = "${when (h) {
                    in 0..9 -> "0$h"
                    else -> h
                }}:${when (mn) {
                    in 0..9 -> "0$mn"
                    else -> mn
                }}"
                findViewById<TextView>(R.id.timerZ1).text = result
            }
            if (data.contains("GetRemainingTimeZ2")) {
                val t = data.split("=").last().toInt()
                println(t)
                val h = t / 60 / 60
                val mn = t / 60 % 60
                val result = "${when (h) {
                    in 0..9 -> "0$h"
                    else -> h
                }}:${when (mn) {
                    in 0..9 -> "0$mn"
                    else -> mn
                }}"
                findViewById<TextView>(R.id.timerZ2).text = result
            }
            if (data.contains("GetTemp;0")) {
                val temp = data.split("=").last().split(":")
                if (temp.count() == 3) {
                    val min = temp.first() + "°C"
                    val max = temp[1] + "°C"
                    val actual = temp.last() + "°C"
                    findViewById<TextView>(R.id.tempIntMin).text = min
                    findViewById<TextView>(R.id.tempIntMax).text = max
                    findViewById<TextView>(R.id.tempIntActual).text = actual
                }
            }
            if (data.contains("GetTemp;1")) {
                val temp = data.split("=").last().split(":")
                if (temp.count() == 3) {
                    val min = temp.first() + "°C"
                    val max = temp[1] + "°C"
                    val actual = temp.last() + "°C"
                    findViewById<TextView>(R.id.tempExtMin).text = min
                    findViewById<TextView>(R.id.tempExtMax).text = max
                    findViewById<TextView>(R.id.tempExtActual).text = actual
                }
            }
            if (data.contains("GetDataCPTEnergy")) {
                var all = data.split("=").last().split("\r").toMutableList()
                all.removeAt(all.count() - 1)
                var daily = 0
                for (value in all) {
                    daily += value.split("|").last().toInt()
                }

                val dailyCons = "${daily/1000.toDouble()}kw/h"
                var dailyCost = "${daily/1000*0.1781}€"
                dailyCost = dailyCost.removeRange(dailyCost.indexOf(".")+3,dailyCost.count()-1)

                findViewById<TextView>(R.id.consDaily).text = dailyCons
                findViewById<TextView>(R.id.dailyCost).text = dailyCost
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 0) {
            if (data != null && data.hasExtra("Ip") && data.hasExtra("Port") && data.hasExtra("Password")) {
                val ip = data.extras?.getString("Ip") ?: String()
                val port = data.extras?.getString("Port") ?: "0"
                val password = data.extras?.getString("Password") ?: String()

                ws.connect(ip, port.toInt(), password)

                while (ws.isOpen()) {
                    if (ws.isReady()) {
                        break
                    }
                }
                if (!ws.isReady()) {
                    val result = when (ws.getLastError()) {
                        NetworkError.PasswordError.ordinal -> getString(R.string.wsPasswordError)
                        NetworkError.DataError.ordinal -> getString(R.string.wsDataError)
                        else -> ws.getLastError().toString()
                    }
                    Toast.makeText(this, result, Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, ConnectionActivity::class.java)
                    intent.putExtra("Path", this.filesDir.path)
                    intent.putExtra("FirstAttempt", false)
                    startActivityForResult(intent, 0)
                } else {
                    Toast.makeText(this, "Connected !", Toast.LENGTH_SHORT).show()
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
