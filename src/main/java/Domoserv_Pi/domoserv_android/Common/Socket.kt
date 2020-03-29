package Domoserv_Pi.domoserv_android.Common

import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import java.nio.ByteBuffer

class socket {
    var serverIP: String = "192.168.1.73"
    var serverPort: Int = 50000
    var password = "passwd"
    var serverAddr = InetAddress.getByName(serverIP)

    var sock = Socket(serverAddr, serverPort)
    var crypto = CryptoFire()

    @ExperimentalStdlibApi
    fun run(): Unit {
        println("Connecting...")
        println(Receipt_Packet())
        println(sock.isConnected)
    }

    private fun To_Packet(value: String): String {
        return "CVOrder|$value"
    }

    private fun To_Value(packet: String): String {
        if(packet.contains("CVOrder|")) {
            return packet.split("CVOrder|").last()
        }
        return "Conversion error"
    }

    @ExperimentalStdlibApi
    private fun Receipt_Packet(): String {
        var end = false
        var message= ""
        var input = sock.getInputStream().bufferedReader(Charsets.UTF_16)
        var output = sock.getOutputStream().bufferedWriter(Charsets.UTF_16)


        if(input.ready() && sock.isConnected) {
            input.skip(3)
            message = input.readLine()
            message = message.trim()

        }
        println("Message received : $message")

        if(message?.split(" ")?.count() == 50) {
            crypto = CryptoFire(50,4,message.toString())
            if(!crypto.Add_Encrypted_Key("Admin",password)) {
                sock.close()
                println("Closing socket")
                return "Socket error, bad password ?"
            }
            println("Socket ready")
            var size: UShort = 0u
            var data = crypto.Encrypt_Data("OKergjeriogjeriogjerzoijgfreoid","Admin")
            println(data)
            size = data.count().toUShort()
            var byte = ByteBuffer.allocate(UShort.SIZE_BYTES + data.count())
            byte.order()

            size = data.count().toUShort()
            var b = size.toByte()
            println(b)



            //byte.put(data.toByteArray(Charsets.UTF_16))
            println(size)
            println(size.toByte())

            output.flush()
            return "Connected !"
        }
        else {
            return To_Value(message.toString())
        }
    }

    private fun Send_Packet(packet: String) {
        sock.outputStream.write(To_Packet(packet).toByteArray())
    }
}