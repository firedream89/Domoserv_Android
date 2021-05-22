package Domoserv_Pi.domoserv_android.Common

import android.R.attr
import kotlinx.android.synthetic.main.activity_connection.view.*
import java.security.MessageDigest
import kotlin.random.Random


//use char key
//User send public key, server return his public key encrypted in user public key + password

//keySize : Défini la taille de la clé de cryptage
//codeSize : défini la difficulté du cryptage basé sur le mot de passe, celui-ci doit être au minimum de la taille du codeSize
class CryptoFire(
    private var keySize: Int,
    private var codeSize: Int,
    private var charFormat: Int,
    private var key: String
) {
    constructor() : this(50, 4, char.UTF8.ordinal, "")

    enum class char {
        UTF8, UTF16
    }
    private var mKey: String = ""
    private var mCodeSize: Int = 0
    private var mEncryptedKeys = mutableListOf("")
    private  var charSize = 0

    init {
        if (keySize <= 0) keySize = 50
        if (codeSize <= 0) codeSize = 4
        mCodeSize = codeSize
        charSize = when (charFormat) {
            char.UTF8.ordinal -> 250
            char.UTF16.ordinal -> 43000
            else -> -1
        }

        if (key.isEmpty()) Generate_Key(keySize)
        else mKey = key

        println("char : $charSize")
        println("codeSize : $codeSize")
        println("key : $mKey")

    }

    fun Get_Key(): String {
        return mKey
    }

    fun Test() {
        println("Start Test")
        println("Valid test key : " + Add_Encrypted_Key("test","Passwdhuihliuhzefz6",""));

        println("fail test same name : " + Add_Encrypted_Key("test","Test2",""))
        println("fail test password too short : " + Add_Encrypted_Key("test2","pas",""))
        println("Valid test key test2 : " + Add_Encrypted_Key("test2","rgreghulihulilh156regregregfze",""))

        println("Test encryption :")
        println("Text 'Ceci est un test' :")
        println("Test 1(test) : ")
        var test = "Ceci est un test"
        test = Encrypt_Data(test, "test")
        print("Encrypted : ")
        println(test)
        print("Decrypted : ")
        test = Decrypt_Data(test, "test");
        println(test)

        print("Test 2(test2) :")
        test = Encrypt_Data(test, "test2");
        print("Encrypted : ")
        println(test)
        print("Decrypted : ")
        test = Decrypt_Data(test, "test2")
        println(test)

        println("Test 3(E=test, D=test2) : ")
        test = Encrypt_Data(test, "test");
        print("Encrypted : ")
        println(test)
        print("Decrypted : ")
        test = Decrypt_Data(test, "test2");
        println(test)
        test = Encrypt_Data(test,"test2");
        test = Decrypt_Data(test,"test");

        println("Test 4(E=test2, D=test) : ")
        println("Decrypted : $test")

        println("Test 5(E=test3) : ")
        test = Encrypt_Data(test,"test3");
        println("Encrypted : $test")

        println("End Test")
    }
    //Création d'une nouvelle clé de cryptage ayant pour nom "name" et suivant le mot de passe "password"
//le "password" doit être >= au codeSize
    fun Add_Encrypted_Key(name: String, password: String, key: String): Boolean {
        for (i in 0 until mEncryptedKeys.count()) {
            if (mEncryptedKeys[i].contains(name)) {
                println("Une clé possède déjà ce nom !")
                return false
            }
        }
        if (password.count() < mCodeSize) {
            println("Le mot de passe est de taille inférieur à $mCodeSize")
            return false
        }

        var result: String = key
        if (result.isEmpty()) {
            result = mKey
        }

        result = Encrypt_Key(password, result)
        if (result.isEmpty() || result.count() > mKey.count()) {
            println("Echec de création de la clé !")
            return false
        }

        mEncryptedKeys.add("$name|$result")

        println("Key to hex(" + name + ") : " + Key_To_SHA256(name))
        return true
    }

    //Suppression de la clé de cryptage "name"
            fun Remove_Encrypted_Key(name: String): Boolean {
                for (i in 0 until mEncryptedKeys.count()) {
            if (mEncryptedKeys[i].contains(name)) {
                mEncryptedKeys.removeAt(i)
                return true
            }
        }
        return false
    }

    //Décryptage des données "data" avec la clé correspondante "name"
    fun Decrypt_Data(data: String, name: String): String {
        //Obtention de la clé de cryptage
        var k = ""
        for (i in 0 until mEncryptedKeys.count()) {
            if (mEncryptedKeys[i].split("|").first() == name) {
                k = mEncryptedKeys[i].split("|").last()
            }
        }
        if (k.isEmpty()) {
            return "Error key not found"
        }

        //décryptage des données
        var decrypt = ""
        var idk = 0
        for (i in 0 until data.count()) {
            if (idk == k.count()) {
                idk = 0
            }
            var t = data[i].toInt()
            if (t == charSize + 1) {//retour a '
                t = 34
            } else if (t == charSize + 2) {//retour a "
                t = 39
            }
            t += k[idk].toInt()
            if (t < 0) {
                t += charSize
            } else if (t > charSize) {
                t -= charSize
            }
            decrypt += t.toChar()
            idk++
        }
        println("Decrypted : $decrypt")
        return decrypt
    }


    //Cryptage des données "data" avec la clé correspondante "name"
    fun Encrypt_Data(data: String, name: String): String {
        //Obtention de la clé de cryptage
        var k = ""
        for (i in 0 until mEncryptedKeys.count()) {
            if (mEncryptedKeys[i].split("|").first() == name) {
                k = mEncryptedKeys[i].split("|").last()
            }

        }
        if (k.isEmpty()) {
            return "Error key not found"
        }

        //cryptage des données
        var crypt = ""
        var idk = 0
        for (i in 0 until data.count()) {
            if (idk == k.count()) {
                idk = 0
            }
            var t = data[i].toInt()
            t -= k[idk].toInt()
            if (t > charSize) {
                t -= charSize
            } else if (t < 0) {
                t += charSize
            }
            if (t == 34) {//si '
                t = charSize + 1
            } else if (t == 39) {//si "
                t = charSize + 2
            }
            crypt += t.toChar()
            idk++
        }
        return crypt
    }

    //Génération de la clé original de la taille "keySize"
    //Clé généré par le serveur
    //Cette clé sera transmise au clients
    private fun Generate_Key(keySize: Int) {
        var key = ""

        for (i in 0 until keySize) {
            key += Random.nextInt(0, charSize).toChar()
        }

        mKey = key
    }

    //Hash String
    private fun hashString(type: String, input: String): String {
        val HEX_CHARS = "0123456789abcdef"
        val bytes = MessageDigest
            .getInstance(type)
            .digest(input.toByteArray())
        val result = StringBuilder(bytes.size * 2)

        bytes.forEach {
            val i = it.toInt()
            result.append(HEX_CHARS[i shr 4 and 0x0f])
            result.append(HEX_CHARS[i and 0x0f])
        }
        return result.toString()
    }

    fun Key_To_SHA256(name: String): String {
        println("Get_Encrypted_Key : " + Get_Encrypted_Key(name))
        return hashString("SHA-256", Get_Encrypted_Key(name))
    }

    //Génération d'une nouvelle clé suivant le mot de passe et la clé original
    //Clé privé non transmise, sera utilisé pour crypter/décrypter les données
    //Clé généré par le client et le serveur
    @ExperimentalUnsignedTypes
    private fun Encrypt_Key(password: String, key: String): String {
        //le mot de passe  doit être minimum de taille identique au "codeSize"
        if (password.count() < mCodeSize) {
            return "Error : WebPassword is too short !"
        }

        var result = key
        if (result.isEmpty()) {
            result = mKey
        }

        //Conversion password to SHA256
        var passwordHash = hashString("SHA-256", password)

        //Génération du code
        val code = Array<Int>(mCodeSize) { 0 }
        for (i in 0 until mCodeSize) {
            code[i] = 0
        }

        var split: Int = passwordHash.count() / mCodeSize
        var act: Int = 0
        for (i in 0 until passwordHash.count()) {
            act++
            if (act > split) {
                act = 0
            }
            code[i / split] += passwordHash[i].toInt()
        }
        for (i in 0 until mCodeSize) {
            code[i] = code[i] % 3
        }

        var codeValue: String = ""
        for (i in 0 until mCodeSize) {
            codeValue += code[i]
        }

        //Génération de la clé suivant code et password
        var ekey = ""
        var intCode = 0
        var intPassword = 0
        for (i in 0 until result.count()) {
            var tchar: UInt
            tchar = if (code[intCode] == 0) {
                result[i].toInt().toUInt() + passwordHash[intPassword].toInt().toUInt()
            } else if (code[intCode] == 1) {
                result[i].toInt().toUInt() - passwordHash[intPassword].toInt().toUInt()
            } else if (code[intCode] == 2) {
                result[i].toInt().toUInt() * passwordHash[intPassword].toInt().toUInt()
            } else if (code[intCode] == 3) {
                result[i].toInt().toUInt() / passwordHash[intPassword].toInt().toUInt()
            } else {
                return "EncryptPKEY : Code is corrupted ! key not encrypted !"
            }

            //Modification de la valeur si supérieur à charSize
            if (tchar > charSize.toUInt()) {
                tchar = tchar % charSize.toUInt()
            }

            ekey += tchar.toInt().toChar()

            intCode++
            if (intCode >= mCodeSize) {
                intCode = 0
            }
            intPassword++;
            if (intPassword >= passwordHash.count()) {
                intPassword = 0
            }
        }

        return if(ekey.count() == result.count()) {
            ekey
        } else {
            "Echec de la génération de la nouvelle clé !";
        }
    }

    fun Get_Encrypted_Key(name: String): String {
        for (i in 0 until mEncryptedKeys.count()) {
            if (mEncryptedKeys[i].split("|").first() == name) {
                return mEncryptedKeys[i].split("|").last();
            }
        }
        return ""
    }
}