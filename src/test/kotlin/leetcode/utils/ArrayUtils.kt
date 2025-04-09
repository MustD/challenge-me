package leetcode.utils

object ArrayUtils {

    fun String.arraySplit() = replace("\"", "").replace("[", "").replace("]", "").split(",")
    fun String.array2arraySplit() = run {
        if (this.isEmpty()) return@run emptyList<List<String>>()
        if (this == "[]") return@run listOf<List<String>>()
        replace("\"", "").replace("[[", "").replace("]]", "").split("],[").map { it.split(",") }
    }

    fun String.toIntArray() = arraySplit().map { it.toInt() }.toIntArray()
    fun String.toDoubleArray() = arraySplit().map { it.toDouble() }.toDoubleArray()

    /**
     * Replace string like [[1,2],[1,3],[1,4],[2,3],[2,4],[3,4]]
     * into List<List<Int>>
     */
    fun String.toListOfIntLists() = array2arraySplit().map { it.map { int -> int.toInt() } }
}
