package cases.sandbox

import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

class SemPool(initialPermits: Int) {

    init {
        require(initialPermits >= 0) { "Permits must be non-negative, was: $initialPermits" }
    }

    private val pool = Semaphore(initialPermits)
    suspend fun <T> use(body: () -> T): T = pool.withPermit { body() }
}
