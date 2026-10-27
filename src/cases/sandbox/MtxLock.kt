package cases.sandbox

import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class MtxLock {
    private val mutex = Mutex()
    suspend fun <T> use(body: () -> T): T = mutex.withLock(currentCoroutineContext()[Job] ?: Any()) { body() }
}
