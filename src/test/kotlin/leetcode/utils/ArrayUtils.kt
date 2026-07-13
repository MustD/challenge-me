package leetcode.utils

import leetcode.utils.ArrayUtils.arraySplit
import leetcode.utils.ArrayUtils.stripStructuralWhitespace


/**
 * A utility object that provides helper functions to manipulate and transform arrays or list-like string representations.
 */
object ArrayUtils {

    /**
     * Removes structural whitespace — spaces, tabs and line breaks — that sits *outside* of
     * double-quoted segments, so that multiline / indented LeetCode literals parse identically
     * to their single-line equivalents.
     *
     * Whitespace *inside* `"..."` is preserved, because string elements may legitimately contain
     * spaces (e.g. `["a b", "c"]`). Only the whitespace between structural tokens (brackets,
     * commas, unquoted values) is dropped.
     *
     * Example: a `"""..."""` matrix literal split across several indented lines collapses back to
     * a single logical line, letting the `],[` / `[[` / `]]` markers match again.
     */
    fun String.stripStructuralWhitespace(): String {
        val sb = StringBuilder(length)
        var inQuotes = false
        for (c in this) {
            when {
                c == '"' -> {
                    inQuotes = !inQuotes; sb.append(c)
                }

                inQuotes -> sb.append(c)
                c.isWhitespace() -> Unit // drop structural whitespace
                else -> sb.append(c)
            }
        }
        return sb.toString()
    }

    /**
     * Splits the string representation of an array-like structure into a list of strings.
     *
     * This function removes double quotes, square brackets, and then splits the string using commas as delimiters.
     * It is commonly used to parse stringified array representations into individual elements.
     *
     * Whitespace, tabs and line breaks between tokens are tolerated — they are stripped via
     * [stripStructuralWhitespace] before parsing (whitespace inside quoted elements is kept).
     *
     * Edge cases:
     * - An empty array representation ("[]") yields an empty list. This is checked before quotes
     *   are stripped so it stays distinct from an array holding a single empty string ("[\"\"]"),
     *   which correctly yields a list containing one empty string.
     *
     * @receiver String representing an array-like structure, e.g., "[1,2,3]" or "\"[a,b,c]\"".
     * @return A list of strings representing the parsed elements from the input string.
     */
    fun String.arraySplit(): List<String> {
        val cleaned = stripStructuralWhitespace()
        if (cleaned == "[]") return emptyList()
        return cleaned.replace("\"", "").replace("[", "").replace("]", "").split(",")
    }

    /**
     * Converts a string representation of a 2D array-like structure into a nested list of strings.
     *
     * This function parses a string representation of a 2D array (e.g., "[[1,2],[3,4]]") into a list of lists of strings,
     * removing unnecessary characters such as double quotes and square brackets.
     * Each inner list represents a row in the 2D array with its corresponding elements split by commas.
     *
     * Edge cases:
     * - If the input string is empty, the function returns an empty list.
     * - If the input string is "[]", the function returns an empty list (zero rows).
     * - An empty *inner* list (e.g. the `[]` in "[[],[1]]") yields an empty row rather than a row
     *   holding a single empty string. After bracket-stripping, an empty row collapses to "" between
     *   the "],[" delimiters; splitting "" on "," would wrongly produce `[""]`, so it is special-cased
     *   to `emptyList()`. This lets subsets-style answers that include the empty set parse correctly.
     *
     * @receiver A string representing a 2D array-like structure.
     * @return A nested list of strings representing the parsed elements from the 2D array.
     */
    fun String.array2arraySplit() = run {
        val cleaned = stripStructuralWhitespace()
        if (cleaned.isEmpty()) return@run emptyList<List<String>>()
        if (cleaned == "[]") return@run listOf<List<String>>()
        cleaned.replace("\"", "").replace("[[", "").replace("]]", "").split("],[")
            .map { if (it.isEmpty()) emptyList() else it.split(",") }
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
     * Converts a string representation of an array-like structure into an array of strings.
     *
     * This function leverages `arraySplit()` to parse the input string into individual elements,
     * trims surrounding whitespace from each element, and returns them as an `Array<String>`.
     *
     * @receiver A string representing an array-like structure, e.g., `["a", "b", "c"]`.
     * @return An `Array<String>` containing the parsed, trimmed elements.
     */
    fun String.toStringArray() = arraySplit().map { it.trim() }.toTypedArray()

    /**
     * Replace string like [[1,2],[1,3],[1,4],[2,3],[2,4],[3,4]]
     * into List<List<Int>>
     */
    fun String.toListOfIntLists() = array2arraySplit().map { it.map { int -> int.toInt() } }

    fun String.toListOfStringLists() = array2arraySplit()

    /**
     * Converts a string representation of a char array (e.g. `["a","a","b"]`) into a [CharArray].
     *
     * Each element is expected to be a single character (LeetCode `char[]` form). Leverages
     * [arraySplit] for parsing, so structural whitespace and the `"[]"` empty-array edge case are
     * handled the same way as the other array parsers.
     *
     * @receiver A string representing a char array, e.g. `["a","b","c"]`.
     * @return A [CharArray] containing the parsed characters.
     */
    fun String.toCharArray() = arraySplit().map { it.trim()[0] }.toCharArray()

    fun String.toCharArray2D() = array2arraySplit().map { row -> row.map { it.trim()[0] }.toCharArray() }.toTypedArray()

    fun String.toIntArray2D() =
        array2arraySplit().map { row -> row.map { it.trim().toInt() }.toIntArray() }.toTypedArray()
}
