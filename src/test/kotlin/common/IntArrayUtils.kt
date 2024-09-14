package common

object IntArrayUtils {

    fun String.toIntArray() = this.replace("[", "").replace("]", "").split(",").map { it.toInt() }.toIntArray()
}