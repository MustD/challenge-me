package common

object ArrayUtils {

    private fun String.arraySplit() = this.replace("[", "").replace("]", "").split(",")

    fun String.toIntArray() = arraySplit().map { it.toInt() }.toIntArray()
    fun String.toDoubleArray() = arraySplit().map { it.toDouble() }.toDoubleArray()

    /**
     * Replace string like [[1,2],[1,3],[1,4],[2,3],[2,4],[3,4]]
     * into List<List<Int>>
     */
    fun String.toListOfIntLists() =
        replace("[[", "").replace("]]", "").split("],[")
            .map { it.split(",").map { int -> int.toInt() } }
}