package Domoserv_Pi.domoserv_android

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity


class ConnectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_connection)

        val button = findViewById<Button>(R.id.connect)
        button.setOnClickListener { this.finish() }
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
}
