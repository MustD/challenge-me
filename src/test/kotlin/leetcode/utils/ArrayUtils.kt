package leetcode.utils

/**
 * A utility object that provides helper functions to manipulate and transform arrays or list-like string representations.
 */
object ArrayUtils {

    /**
     * Splits the string representation of an array-like structure into a list of strings.
     *
     * This function removes double quotes, square brackets, and then splits the string using commas as delimiters.
     * It is commonly used to parse stringified array representations into individual elements.
     *
     * @receiver String representing an array-like structure, e.g., "[1,2,3]" or "\"[a,b,c]\"".
     * @return A list of strings representing the parsed elements from the input string.
     */
    fun String.arraySplit() = replace("\"", "").replace("[", "").replace("]", "").split(",")

    /**
     * Converts a string representation of a 2D array-like structure into a nested list of strings.
     *
     * This function parses a string representation of a 2D array (e.g., "[[1,2],[3,4]]") into a list of lists of strings,
     * removing unnecessary characters such as double quotes and square brackets.
     * Each inner list represents a row in the 2D array with its corresponding elements split by commas.
     *
     * Edge cases:
     * - If the input string is empty, the function returns an empty list.
     * - If the input string is "[]", the function returns a list containing an empty list.
     *
     * @receiver A string representing a 2D array-like structure.
     * @return A nested list of strings representing the parsed elements from the 2D array.
     */
    fun String.array2arraySplit() = run {
        if (this.isEmpty()) return@run emptyList<List<String>>()
        if (this == "[]") return@run listOf<List<String>>()
        replace("\"", "").replace("[[", "").replace("]]", "").split("],[").map { it.split(",") }
    }

    /**
     * Converts a string representation of an array-like structure into an array of integers.
     *
     * This function leverages the `arraySplit` function to parse the input string into individual elements,
     * converts each element to an integer, and finally returns these integers as an `IntArray`.
     *
     * Use this function when you need to transform a stringified array representation (e.g., "[1,2,3]")
     * into a strongly typed `IntArray`.
     *
     * @receiver A string representing an array-like structure, where elements are separated by commas
     *           and enclosed in square brackets or double quotes.
     * @return An `IntArray` containing integer values parsed from the input string.
     * @throws NumberFormatException If any of the elements in the string cannot be converted to an integer.
     */
    fun String.toIntArray() = arraySplit().map { it.toInt() }.toIntArray()

    /**
     * Converts a string representation of a numeric array-like structure into a DoubleArray.
     *
     * This function first splits the string into individual elements using `arraySplit()`,
     * then maps each element into a Double, and finally converts the resulting list into a DoubleArray.
     *
     * @receiver String representing an array-like structure, such as "[1.0,2.5,3.0]".
     * @return A DoubleArray containing the parsed numeric values.
     * @throws NumberFormatException if any of the elements cannot be parsed into a Double.
     */
    fun String.toDoubleArray() = arraySplit().map { it.toDouble() }.toDoubleArray()

    /**
     * Replace string like [[1,2],[1,3],[1,4],[2,3],[2,4],[3,4]]
     * into List<List<Int>>
     */
    fun String.toListOfIntLists() = array2arraySplit().map { it.map { int -> int.toInt() } }
}
