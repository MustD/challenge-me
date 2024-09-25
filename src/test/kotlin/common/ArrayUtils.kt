package common

object ArrayUtils {

    private fun String.arraySplit() = this.replace("[", "").replace("]", "").split(",")

    fun String.toIntArray() = arraySplit().map { it.toInt() }.toIntArray()
    fun String.toDoubleArray() = arraySplit().map { it.toDouble() }.toDoubleArray()
}