import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * https://leetcode.com/problems/apply-operations-on-array-to-maximize-sum-of-squares
 */
class I2897applyOperationsOnArrayToMaximizeSumOfSquares {
    data class Case(
        val nums: List<Int>,
        val k: Int,
        val output: Int,
    )

    @Nested
    inner class Solution : AproblemTest<Case, (List<Int>, Int) -> Int> {
        override val cases: List<Case> = listOf(
            Case(listOf(2, 6, 5, 8), 2, 261),
            Case(listOf(4, 5, 4, 7), 3, 90),
            Case(listOf(96, 66, 60, 58, 32, 17, 63, 21, 30, 44, 15, 8, 98, 93), 2, 32258),
            Case(listOf(25, 52, 75, 65), 4, 24051),
            Case(data5, 163, 683004067),

            )

        override val solutions: List<Pair<String, (List<Int>, Int) -> Int>> = listOf(
            "solution 1" to ::solution1,
            "solution 2" to ::solution2,
            "III" to ::solution3,
        )

        override fun Case.check(solution: (List<Int>, Int) -> Int): Pair<Boolean, Any> {
            val result = solution(nums, k)
            val isCorrect = result == output
            return isCorrect to result
        }

        @Test
        fun test() = check()

        private fun List<Int>.print(): List<Int> {
            println("--- list $this ---")
            forEach { println(it.toString(2)) }
            println("------------------")
            return this
        }

        //too long
        fun solution1(nums: List<Int>, k: Int): Int {
            fun List<Int>.operation(i: Int, j: Int): List<Int> = mapIndexed { idx, value ->
                when (idx) {
                    i -> get(i) and get(j)
                    j -> get(i) or get(j)
                    else -> value
                }
            }

            fun List<Int>.calculation() = takeLast(k).sumOf { it * it }

            var result: List<Int> = nums.sorted()
            repeat(k) { kIdx ->
                repeat(nums.size) { idx ->
                    result = result.operation(idx, result.lastIndex - kIdx)
                }
            }

            return result.calculation() % 1000000007
        }

        fun solution2(nums: List<Int>, k: Int): Int {
            fun MutableList<Int>.operation(i: Int, j: Int): MutableList<Int> {
                if (i == j) return this
                val iVal = get(i)
                val jVal = get(j)
                set(i, iVal and jVal)
                set(j, iVal or jVal)
                return this
            }

            fun List<Int>.calculation() = takeLast(k).map { it.toLong() }.sumOf { it * it }

            var result: MutableList<Int> = nums.toMutableList()
            nums.indices.map { idx ->
                repeat(k) kLoop@{ kIdx ->
                    if (result[idx] == 0) return@map
                    if (result.lastIndex - kIdx < 0) return@kLoop
                    result = result.operation(idx, result.lastIndex - kIdx)
                }
            }

            return (result.calculation() % 1000000007).toInt()

        }

        fun solution3(nums: List<Int>, k: Int): Int {
            fun MutableList<Int>.operation(i: Int, j: Int): MutableList<Int> {
                if (i == j) return this
                val iVal = get(i)
                val jVal = get(j)
                set(i, iVal and jVal)
                set(j, iVal or jVal)
                return this
            }

            fun List<Int>.calculation() = takeLast(k).map { it.toLong() }.sumOf { it * it }

            var result: MutableList<Int> = nums.toMutableList()

            repeat(k) {
                result.indices.map { idx ->
                    if (idx + 1 > result.lastIndex) return@map
                    result = result.operation(idx, idx + 1)
                }
            }

            return (result.calculation() % 1000000007).toInt()
        }
    }

}

val data5 = listOf(
    196,
    1295,
    176,
    1817,
    958,
    796,
    1480,
    1934,
    1647,
    62,
    736,
    1818,
    1554,
    232,
    1113,
    1657,
    1164,
    168,
    1859,
    1375,
    1189,
    917,
    1931,
    1513,
    558,
    85,
    1430,
    191,
    1287,
    638,
    1082,
    483,
    773,
    852,
    75,
    983,
    95,
    1857,
    1541,
    1662,
    1091,
    1773,
    703,
    101,
    661,
    1814,
    1243,
    748,
    778,
    1806,
    1148,
    1856,
    849,
    772,
    1684,
    1101,
    1193,
    1358,
    790,
    1933,
    1887,
    1581,
    751,
    385,
    313,
    791,
    1755,
    563,
    1603,
    915,
    1121,
    237,
    1559,
    544,
    1322,
    747,
    1507,
    752,
    1839,
    1876,
    164,
    1248,
    103,
    1196,
    788,
    43,
    1221,
    1334,
    791,
    1136,
    1705,
    439,
    1036,
    255,
    1577,
    815,
    279,
    1435,
    1226,
    1186,
    1996,
    135,
    522,
    1969,
    1607,
    512,
    1815,
    722,
    1198,
    13,
    571,
    349,
    644,
    851,
    349,
    12,
    1866,
    1878,
    1348,
    1312,
    145,
    1028,
    327,
    954,
    1926,
    383,
    649,
    1694,
    1652,
    480,
    1943,
    1848,
    1301,
    1175,
    1594,
    1790,
    387,
    873,
    977,
    1570,
    1292,
    1517,
    1052,
    734,
    1626,
    646,
    1310,
    771,
    1220,
    1690,
    836,
    694,
    1247,
    1814,
    235,
    801,
    1625,
    719,
    1122,
    445,
    1294,
    1097,
    1792,
    25,
    37,
    1983,
    1945,
    1781,
    1569,
    1701,
    265,
    450,
    191,
    488,
    535,
    1482,
    895,
    1596,
    1087,
    1676,
    1012,
    1957,
    420,
    734,
    920,
    656,
    1467,
    1857,
    1603,
    920,
    447,
    1421,
    476,
    1464,
    1727,
    1988,
    1253,
    1626,
    39,
    999,
    714,
    514,
    87,
    952,
    683,
    1654,
    94,
    249,
    423,
    932,
    76,
    1951,
    1321,
    1953,
    1637,
    847,
    173,
    1422,
    556,
    1189,
    1968,
    1095,
    341,
    1778,
    1894,
    1799,
    672,
    400,
    1899,
    844,
    992,
    1277,
    176,
    1070,
    1089,
    151,
    1716,
    234,
    483,
    1953,
    851,
    482,
    1372,
    1056,
    487,
    1544,
    816,
    45,
    206,
    1859,
    500,
    1508,
    632,
    1602,
    1953,
    372,
    472,
    1382,
    781,
    1171,
    990,
    269,
    541,
    586,
    301,
    337,
    655,
    172,
    1003,
    1388,
    204,
    1532,
    1665,
    128,
    473,
    1852,
    900,
    1593,
    839,
    156,
    754,
    499,
    1916,
    1754,
    432,
    1042,
    1856,
    391,
    1908,
    1253,
    362,
    301,
    1591,
    1881,
    1375,
    711,
    1136,
    1691,
    1178,
    1665,
    1516,
    1259,
    1130,
    1383,
    1987,
    1661,
    434,
    150,
    175,
    64,
    1777,
    1995,
    662,
    814,
    694,
    1865,
    897,
    955,
    1243,
    1549,
    1083,
    739,
    1210,
    1837,
    1394,
    1267,
    1207,
    884,
    403,
    963,
    1802,
    1678,
    1049,
    1526,
    478,
    587,
    619,
    741,
    626,
    1940,
    226,
    1786,
    1804,
    310,
    749,
    265,
    1383,
    1403,
    1847,
    447,
    1794,
    113,
    1681,
    1475,
    1428,
    1727,
    1100,
    1718,
    720,
    873,
    1855,
    1529,
    730,
    671,
    71,
    1947,
    602,
    439,
    909,
    280,
    1703,
    358,
    1262,
    1990,
    1243,
    1866,
    1901,
    49,
    1651,
    400,
    1610,
    1345,
    1577,
    1530,
    198,
    941,
    1619,
    1031,
    1940,
    501,
    1172,
    1034,
    252,
    789,
    463,
    387,
    1959,
    1381,
    1816,
    1185,
    573,
    1208,
    1435,
    1464,
    1456,
    1871,
    1157,
    775,
    1086,
    712,
    905,
    212,
    526,
    1971,
    158,
    1798,
    1682,
    1826,
    1531,
    673,
    760
)
