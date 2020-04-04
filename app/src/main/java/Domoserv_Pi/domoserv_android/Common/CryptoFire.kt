package Domoserv_Pi.domoserv_android.Common

import kotlin.random.Random

//use char key
//User send public key, server return his public key encrypted in user public key + password

//keySize : Défini la taille de la clé de cryptage
//codeSize : défini la difficulté du cryptage basé sur le mot de passe, celui-ci doit être au minimum de la taille du codeSize
class CryptoFire(private var key: String) {
    private var codeSize: Int = 4
    private var keySize: Int = 50

    private var myEncryptedKeys = mutableListOf("")

    init {
        this.key = if (this.key.isEmpty()) generateKey(keySize) else this.key
    }

    //Création d'une nouvelle clé de cryptage ayant pour nom "name" et suivant le mot de passe "password"
//le "password" doit être >= au codeSize
    @ExperimentalUnsignedTypes
    fun addEncryptedKey(name: String, password: String): Boolean {
        for (i in 0 until this.myEncryptedKeys.count()) {
            if (this.myEncryptedKeys[i].contains(name)) {
                println("Une clé possède déjà ce nom !")
                return false
            }
        }
        if (password.count() < this.codeSize) {
            println("Le mot de passe est de taille inférieur à ${this.codeSize}")
            return false
        }
        val result: String = encryptKey(password)
        if (result.isEmpty()) {
            println("Echec de création de la clé !")
            return false
        }

        this.myEncryptedKeys.add("$name|$result")
        return true
    }

    //Suppression de la clé de cryptage "name"
    fun removeEncryptedKey(name: String): Boolean {
        for (i in 0 until this.myEncryptedKeys.count()) {
            if (this.myEncryptedKeys[i].contains(name)) {
                this.myEncryptedKeys.removeAt(i)
                return true
            }
        }
        return false
    }

    //Décryptage des données "data" avec la clé correspondante "name"
    fun decryptData(data: String, name: String): String {
        //Obtention de la clé de cryptage
        var k = listOf("")
        for (i in 0 until this.myEncryptedKeys.count()) {
            if (this.myEncryptedKeys[i].split("|").first() == name) {
                k = this.myEncryptedKeys[i].split("|").last().split(" ")
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
            if (t == 251) {//retour a '
                t = 34
            }
            if (t == 252) {//retour a "
                t = 39
            }
            t += k[idk].toInt()
            if (t < 0) {
                t += 250
            }
            if (t > 250) {
                t -= 250
            }
            decrypt += t.toChar()
            idk++
        }

        return decrypt
    }


    //Cryptage des données "data" avec la clé correspondante "name"
    fun encryptData(data: String, name: String): String {
        //Obtention de la clé de cryptage
        var k = listOf<String>()
        for (i in 0 until this.myEncryptedKeys.count()) {
            if (this.myEncryptedKeys[i].split("|").first() == name) {
                k = this.myEncryptedKeys[i].split("|").last().split(" ")
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
            if (t > 250) {
                t -= 250
            }
            if (t < 0) {
                t += 250
            }
            if (t == 34) {//si '
                t = 251
            }
            if (t == 39) {//si "
                t = 252
            }
            crypt += t.toChar()
            idk++
        }
        return crypt
    }

    //Génération de la clé original de la taille "keySize"
    //Clé généré par le serveur
    //Cette clé sera transmise au clients
    private fun generateKey(keySize: Int): String {
        var key = ""

        for (i in 0 until keySize) {
            key += Random.nextInt(0, 250).toString()
            if (key.split(" ").count() < keySize)
                key += " "
        }

        return key
    }

    //Génération d'une nouvelle clé suivant le mot de passe et la clé original
    //Clé privé non transmise, sera utilisé pour crypter/décrypter les données
    //Clé généré par le client et le serveur
    @ExperimentalUnsignedTypes
    private fun encryptKey(password: String): String {
        //le mot de passe  doit être minimum de taille identique au "codeSize"
        if (password.count() < this.codeSize) {
            return "Error : WebPassword is too short !"
        }

        //Génération du code
        val code = generateCode(password)

        //Génération de la clé suivant code et password
        return generateKey(password, code)
    }

    private fun generateCode(password: String): Array<Int> {
        val code = emptyArray<Int>()
        Array(this.codeSize) { 0 }
        for (i in 0 until this.codeSize) {
            code[i] = Character.getNumericValue(password[i]) % 3
        }
        return code
    }

    @ExperimentalUnsignedTypes
    private fun generateKey(password: String, code: Array<Int>): String {
        var ekey = ""
        var intCode = 0
        var intPassword = 0
        for (i in 0 until this.key.split(" ").count()) {
            var tchar: UInt
            tchar = when (code[intCode]) {
                0 -> this.key.split(" ")[i].toUInt() + password[intPassword].toInt().toUInt()
                1 -> this.key.split(" ")[i].toUInt() - password[intPassword].toInt().toUInt()
                2 -> this.key.split(" ")[i].toUInt() * password[intPassword].toInt().toUInt()
                3 -> this.key.split(" ")[i].toUInt() / password[intPassword].toInt().toUInt()
                else -> 0U
            }

            if (tchar == 0U) throw Exception("EncryptPKEY : Code is corrupted ! key not encrypted !")

            //Modification de la valeur si supérieur à 250
            if (tchar > 250u) {
                tchar = tchar % 250u
            }
            if (i == this.key.split(" ").count() - 1)
                ekey += tchar
            else
                ekey += "$tchar "
            intCode++
            if (intCode >= this.codeSize) {
                intCode = 0
            }
            intPassword++
            if (intPassword >= password.count()) {
                intPassword = 0
            }
        }

        return if (ekey.split(" ").count() == this.key.split(" ").count()) {
            ekey
        } else {
            "Echec de la génération de la nouvelle clé !"
        }

    }
}