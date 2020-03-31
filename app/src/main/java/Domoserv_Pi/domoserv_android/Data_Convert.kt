package Domoserv_Pi.domoserv_android

class Data_Convert {

    fun Data_To_String(data: String): String {
        return data.split("|").last()
    }

    fun String_To_Data(text: String): String {
        return "CVOrder|$text"
    }

    fun To_Time(value: Int): String {
        return String()
    }

    fun From_Time(value: String): Int {
        return 0
    }

    fun To_List(text: String, separator: String): List<String> {
        return text.split(separator)
    }
}