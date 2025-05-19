package leetcode.array_string

class I0380RandomizedSet {

    val set = mutableSetOf<Int>()

    fun insert(`val`: Int): Boolean {
        return set.add(`val`)
    }

    fun remove(`val`: Int): Boolean {
        return set.remove(`val`)
    }

    fun getRandom(): Int {
        return set.random()
    }


}

/**
 * Your RandomizedSet object will be instantiated and called as such:
 * var obj = RandomizedSet()
 * var param_1 = obj.insert(`val`)
 * var param_2 = obj.remove(`val`)
 * var param_3 = obj.getRandom()
 */
