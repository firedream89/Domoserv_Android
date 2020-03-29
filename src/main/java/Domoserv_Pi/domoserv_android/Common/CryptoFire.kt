package Domoserv_Pi.domoserv_android.Common

import kotlin.random.Random


//use char key
//User send public key, server return his public key encrypted in user public key + password

var _key: String = ""
var _codeSize: Int = 0
var _EncryptedKeys = mutableListOf("")

//keySize : Défini la taille de la clé de cryptage
//codeSize : défini la difficulté du cryptage basé sur le mot de passe, celui-ci doit être au minimum de la taille du codeSize
class CryptoFire(private var keySize: Int, private var codeSize: Int, private var key: String) {
    constructor() : this(50, 4, "")
    init {
        if (keySize <= 0) keySize = 50
        if (codeSize <= 0) codeSize = 4
        _codeSize = codeSize
        if (key.isEmpty()) Generate_Key(keySize)
        else _key = key
    }

    fun Get_Key(): String {
        return _key
    }

    //Création d'une nouvelle clé de cryptage ayant pour nom "name" et suivant le mot de passe "password"
//le "password" doit être >= au codeSize
    fun Add_Encrypted_Key(name: String, password: String): Boolean {
        for (i in 0 until _EncryptedKeys.count()) {
            if (_EncryptedKeys[i].contains(name)) {
                println("Une clé possède déjà ce nom !")
                return false
            }
        }
        if (password.count() < _codeSize) {
            println("Le mot de passe est de taille inférieur à $_codeSize")
            return false
        }
        val result: String = Encrypt_Key(password)
        if (result.isEmpty()) {
            println("Echec de création de la clé !")
            return false
        }

        _EncryptedKeys.add("$name|$result")
        return true
    }

    //Suppression de la clé de cryptage "name"
    fun Remove_Encrypted_Key(name: String): Boolean {
        for (i in 0 until _EncryptedKeys.count()) {
            if (_EncryptedKeys[i].contains(name)) {
                _EncryptedKeys.removeAt(i)
                return true
            }
        }
        return false
    }

    //Décryptage des données "data" avec la clé correspondante "name"
    fun Decrypt_Data(data: String, name: String): String {
        //Obtention de la clé de cryptage
        var k = listOf<String>("")
        for (i in 0 until _EncryptedKeys.count()) {
            if (_EncryptedKeys[i].split("|").first() == name) {
                k = _EncryptedKeys[i].split("|").last().split(" ")
            }
        }
        if (k.isEmpty()) {
            return "Error key not found"
        }

        //décryptage des données
        var decrypt: String = ""
        var idk = 0
        for (i in 0 until data.count()) {
            if (idk == k.count()) {
                idk = 0
            }
            var t = data[i].toInt()
            if (t == 251) {//retour a '
                t = 34
            } else if (t == 252) {//retour a "
                t = 39
            }
            t += k[idk].toInt()
            if (t < 0) {
                t += 250
            } else if (t > 250) {
                t -= 250
            }
            decrypt += t.toChar()
            idk++
        }

        return decrypt
    }


    //Cryptage des données "data" avec la clé correspondante "name"
    fun Encrypt_Data(data: String, name: String): String {
        //Obtention de la clé de cryptage
        var k = listOf<String>()
        for (i in 0 until _EncryptedKeys.count()) {
            if (_EncryptedKeys[i].split("|").first() == name) {
                k = _EncryptedKeys[i].split("|").last().split(" ")
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
            } else if (t < 0) {
                t += 250
            }
            if (t == 34) {//si '
                t = 251
            } else if (t == 39) {//si "
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
    private fun Generate_Key(keySize: Int) {
        var key = ""

        for (i in 0 until keySize) {
            key += Random.nextInt(0, 250).toString()
            if(key.split(" ").count() < keySize)
                key += " "
        }

        _key = key
    }

    //Génération d'une nouvelle clé suivant le mot de passe et la clé original
    //Clé privé non transmise, sera utilisé pour crypter/décrypter les données
    //Clé généré par le client et le serveur
    private fun Encrypt_Key(password: String): String {
        //le mot de passe  doit être minimum de taille identique au "codeSize"
        if (password.count() < _codeSize) {
            return "Error : WebPassword is too short !"
        }

        //Génération du code
        val code = Array<Int>(_codeSize) { 0 }
        for (i in 0 until _codeSize) {
            code[i] = Character.getNumericValue(password[i]) % 3
        }

        //Génération de la clé suivant code et password
        var ekey = ""
        var intCode = 0
        var intPassword = 0
        for (i in 0 until _key.split(" ").count()) {
            var tchar: UInt
            if (code[intCode] == 0) {
                tchar = _key.split(" ")[i].toUInt() + password[intPassword].toInt().toUInt()
            } else if (code[intCode] == 1) {
                tchar = _key.split(" ")[i].toUInt() - password[intPassword].toInt().toUInt()
            } else if (code[intCode] == 2) {
                tchar = _key.split(" ")[i].toUInt() * password[intPassword].toInt().toUInt()
            } else if (code[intCode] == 3) {
                tchar = _key.split(" ")[i].toUInt() / password[intPassword].toInt().toUInt()
            } else {
                return "EncryptPKEY : Code is corrupted ! key not encrypted !"
            }

            //Modification de la valeur si supérieur à 250
            if (tchar > 250u) {
                tchar = tchar % 250u
            }
            if(i == _key.split(" ").count()-1)
                ekey += tchar
            else
                ekey += "$tchar "
            intCode++
            if (intCode >= _codeSize) {
                intCode = 0
            }
            intPassword++;
            if (intPassword >= password.count()) {
                intPassword = 0
            }
        }

        if(ekey.split(" ").count() == _key.split(" ").count()) {
            return ekey
        }
        else {
            return "Echec de la génération de la nouvelle clé !";
        }
    }
}