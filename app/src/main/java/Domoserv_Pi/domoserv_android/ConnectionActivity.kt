package Domoserv_Pi.domoserv_android

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.*


class ConnectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_connection)
        var firstAttempt = true

        //Extract value from parent
        if(intent.hasExtra("FirstAttempt")) {
            firstAttempt = intent.extras?.getBoolean("FirstAttempt") ?: false
        }

        //Extract saved values in file
        val path = intent.extras?.getString("Path")
        if(path != null && path.isNotEmpty()) {
            val file = File("$path/Identifiers.txt")
            if(!file.exists()) {
                file.createNewFile()
            }
            val result = file.readLines()
            println(result)
            for(res in result) {
                if (res.contains("Ip")) {
                    findViewById<EditText>(R.id.ip).setText(res.split("=").last())
                }
                if (res.contains("Port")) {
                    findViewById<EditText>(R.id.port).setText(res.split("=").last())
                }
                if (res.contains("Password")) {
                    findViewById<EditText>(R.id.password).setText(res.split("=").last())
                }
            }

            //close activity if first connection
            if(firstAttempt) {
                if(editIsValid()) {
                    this.finish()
                }
            }
        }

        //On click, Control and send values to parent
        val button = findViewById<Button>(R.id.connect)
        button.setOnClickListener {
            button.isEnabled = false
            if(editIsValid()) {
                this.finish()
            }
            else {
                button.isEnabled = true
            }
        }
    }

    override fun finish() {

        val data = Intent()
        val ip = findViewById<EditText>(R.id.ip).text.toString()
        val port = findViewById<EditText>(R.id.port).text.toString()
        val password = findViewById<EditText>(R.id.password).text.toString()
        val saveIdentifier = findViewById<CheckBox>(R.id.saveIdentifiers).isChecked

        data.putExtra("Ip", ip)
        data.putExtra("Port", port)
        data.putExtra("Password", password)

        if(saveIdentifier) {
            val path = intent.extras?.getString("Path")
            if (path != null && path.isNotEmpty()) {
                val file = File("$path/Identifiers.txt")
                if (!file.exists()) {
                    file.createNewFile()
                } else {
                    file.delete()
                    file.createNewFile()
                }
                file.writeText("Ip=$ip\nPort=$port\nPassword=$password\n")
            }
        }

        setResult(Activity.RESULT_OK, data)
        super.finish()
    }

    private fun editIsValid(): Boolean {
        val ip = findViewById<EditText>(R.id.ip).text.toString()
        val port = findViewById<EditText>(R.id.port).text.toString()
        val password = findViewById<EditText>(R.id.password).text.toString()

        if(ip.isEmpty()) {
            Toast.makeText(this,getString(R.string.connexionIpError), Toast.LENGTH_SHORT).show()
        }
        else if(port.isEmpty() || port.toInt() < 49152 || port.toInt() > 65535) {
            Toast.makeText(this,getString(R.string.connexionPortError), Toast.LENGTH_SHORT).show()
        }
        else if(password.isEmpty()) {
            Toast.makeText(this,getString(R.string.connexionPasswordError), Toast.LENGTH_SHORT).show()
        }
        else {
            return true
        }
        return false
    }
}
