package Domoserv_Pi.domoserv_android

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class ConnectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_connection)

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

        val ip = findViewById<EditText>(R.id.ip)
        val port = findViewById<EditText>(R.id.port)
        val password = findViewById<EditText>(R.id.password)
        data.putExtra("Ip", ip.text.toString())
        data.putExtra("Port", port.text.toString())
        data.putExtra("Password", password.text.toString())

        setResult(Activity.RESULT_OK, data)
        super.finish()
    }

    private fun editIsValid(): Boolean {
        val ip = findViewById<EditText>(R.id.ip).text.toString()
        var port = findViewById<EditText>(R.id.port).text.toString()
        if(port.isEmpty()) {
            port = "0"
        }
        val password = findViewById<EditText>(R.id.password).text.toString()

        if(ip.isEmpty()) {
            Toast.makeText(this,getString(R.string.connexionIpError), Toast.LENGTH_SHORT).show()
        }
        else if(port.toInt() < 49152 || port.toInt() > 65535) {
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
