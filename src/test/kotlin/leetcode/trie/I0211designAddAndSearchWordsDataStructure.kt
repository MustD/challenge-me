package leetcode.trie

/**
 * LeetCode 211 — solved with a Trie. Words are stored char-by-char along shared paths,
 * with an `isWord` end-marker so only complete words match (not mere prefixes). Search
 * walks the trie; a '.' wildcard matches any single letter by recursing into every child.
 */
class I0211designAddAndSearchWordsDataStructure {
    class WordDictionary() {

        data class Node(
            val value: Char, //not used in practice
            var isWord: Boolean = false,
        ) {
            val children: MutableList<Node?> = MutableList(('a'..'z').count()) { null }

        }

        val root = Node(' ')

        fun add(node: Node = root, idx: Int = 0, word: String) {
            if (idx > word.lastIndex) {
                node.isWord = true
                return
            }
            val charIndex = word[idx] - 'a'
            val childNode = node.children[charIndex] ?: Node(word[idx]).also { node.children[charIndex] = it }
            add(childNode, idx + 1, word)
        }

        fun addWord(word: String) {
            add(root, 0, word)
        }

        fun searchWord(node: Node = root, idx: Int = 0, word: String): Boolean {
            if (idx > word.lastIndex) return node.isWord
            val char = word[idx]
            if (char == '.') {
                return node.children.any { it != null && searchWord(it, idx + 1, word) }
            }
            val childNode = node.children[word[idx] - 'a']
            return if (childNode == null) {
                false
            } else {
                searchWord(childNode, idx + 1, word)
            }
        }

        fun search(word: String): Boolean {
            return searchWord(root, 0, word)
        }

    }
}
