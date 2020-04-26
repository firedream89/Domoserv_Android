package Domoserv_Pi.domoserv_android

import Domoserv_Pi.domoserv_android.Common.NetworkError
import Domoserv_Pi.domoserv_android.Common.WebSock
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Window
import android.widget.*
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

        val btStateZ1 = this.findViewById(R.id.stateZ1) as TextView
        findViewById<TextView>(R.id.stateZ1).setOnClickListener {
            showDialog("Zone 1 : State",0, stateList.indexOf(btStateZ1.text))
        }
        val btStateZ2 = this.findViewById(R.id.stateZ2) as TextView
        findViewById<TextView>(R.id.stateZ2).setOnClickListener {
            showDialog("Zone 2 : State",0, stateList.indexOf(btStateZ2.text))
        }
        val btModeZ1 = this.findViewById(R.id.modeZ1) as TextView
        findViewById<TextView>(R.id.modeZ1).setOnClickListener {
            showDialog("Zone 1 : Mode",1, modeList.indexOf(btModeZ1.text))
        }
        val btModeZ2 = this.findViewById(R.id.modeZ2) as TextView
        findViewById<TextView>(R.id.modeZ2).setOnClickListener {
            showDialog("Zone 2 : Mode",1, modeList.indexOf(btModeZ2.text))
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

    private fun showDialog(title: String, type: Int, selected: Int) {
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

        val spinner = dialog.findViewById(R.id.select) as Spinner
        val dataAdapter = when(type) {
            1 -> ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, modeList)
            else -> ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, stateList)
        }
        spinner.adapter = dataAdapter
        spinner.setSelection(selected)

        val submit = dialog.findViewById(R.id.submit) as Button
        submit.setOnClickListener {
            val index = dialog.findViewById<Spinner>(R.id.select).selectedItemPosition
            when(type) {
                1 -> changeMode(zone, index)
                else -> changeState(zone, index)
            }
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun changeState(zone: Int, state: Int) {
        ws.send(getString(R.string.setZoneState).replace("{zone}",zone.toString()).replace("{state}",state.toString()))
        startUpdate()
    }

    private fun changeMode(zone: Int, mode: Int) {
        ws.send(getString(R.string.setZoneMode).replace("{zone}",zone.toString()).replace("{mode}",mode.toString()))
        startUpdate()
    }

    fun startUpdate() {
        ws.send(getString(R.string.getZ1State))
        ws.send(getString(R.string.getZ2State))
        ws.send(getString(R.string.getZ1Mode))
        ws.send(getString(R.string.getZ2Mode))
        ws.send(getString(R.string.getZ1RemainingTime))
        ws.send(getString(R.string.getZ2RemainingTime))
        ws.send(getString(R.string.getIndoorTemp))
        ws.send(getString(R.string.getOutdoorTemp))
        val date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val endDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val dateYear = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy")) + "-01-01"
        ws.send(getString(R.string.getDataEnergy).replace("{date}",date).replace("{endDate}",endDate))
    }

    fun updateField(data: String) {
        if (data.contains(getString(R.string.cvorderValue))) {
            if (data.contains(getString(R.string.getZ1State))) {
                findViewById<TextView>(R.id.stateZ1).text =
                    stateList[data.split("=").last().toInt()]
            }
            if (data.contains(getString(R.string.getZ2State))) {
                findViewById<TextView>(R.id.stateZ2).text =
                    stateList[data.split("=").last().toInt()]
            }
            if (data.contains(getString(R.string.getZ1Mode))) {
                findViewById<TextView>(R.id.modeZ1).text = modeList[data.split("=").last().toInt()]
            }
            if (data.contains(getString(R.string.getZ2Mode))) {
                findViewById<TextView>(R.id.modeZ2).text = modeList[data.split("=").last().toInt()]
            }
            if (data.contains(getString(R.string.getZ1RemainingTime))) {
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
            if (data.contains(getString(R.string.getZ2RemainingTime))) {
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
            if (data.contains(getString(R.string.getIndoorTemp))) {
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
            if (data.contains(getString(R.string.getOutdoorTemp))) {
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
            if (data.contains(getString(R.string.getDataEnergy))) {
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
