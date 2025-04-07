package leetcode.trie

class I0208ImplementTrie {


    class Trie() {
        class Node() {
            var isEnd: Boolean = false
            val children: MutableMap<Char, Node> = mutableMapOf()
        }

        private val root: Node = Node()

        /**
         * Time - O(n) where n is the length of the word
         * Space - O(n) in the worst case
         * Here and below we can replace word param with str index to optimize
         */
        fun insert(word: String) {
            tailrec fun rec(word: String, node: Node) {
                if (word.isEmpty()) {
                    node.isEnd = true
                    return
                }
                val char = word[0]
                val subNode = node.children.getOrPut(char) { Node() }
                return rec(word.drop(1), subNode)
            }
            rec(word, root)
        }

        /**
         * Time - O(n) where n is the length of the word
         * Space - O(1) additional space (excluding the input)
         */
        fun search(word: String): Boolean {
            tailrec fun rec(word: String, node: Node): Boolean {
                if (word.isEmpty()) return node.isEnd
                val char = word[0]
                val subNode = node.children[char] ?: return false
                return rec(word.drop(1), subNode)
            }
            return rec(word, root)
        }

        /**
         * Time - O(n) where n is the length of the word
         * Space - O(1) additional space (excluding the input)
         */
        fun startsWith(prefix: String): Boolean {
            tailrec fun rec(word: String, node: Node): Boolean {
                if (word.isEmpty()) return true
                val char = word[0]
                val subNode = node.children[char] ?: return false
                return rec(word.drop(1), subNode)
            }
            return rec(prefix, root)
        }

    }
}
