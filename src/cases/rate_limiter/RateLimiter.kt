package cases.rate_limiter

import java.time.OffsetDateTime
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid


// user id as user identifier
// multiple api endpoints
// r / p


data class RLRequest(val id: Uuid)
data class RlResponse(val pass: Boolean)

data class RLSettings(val duration: Duration = 1.seconds, val limit: Int = 10)

class RLService(private val settings: RLSettings) {

    data class UserBucket(
        val lastAccess: OffsetDateTime,
        val tokenLeft: Int,
    )

    val maxTokens = RLUtils.calculateMaxTokens(settings)
    val storage = ConcurrentHashMap<Uuid, UserBucket>()

    // userId : timeStamp, tokenCount
    // validate : duration for now() - timeStamp / unit * token (not more that max)

    fun validateRate(req: RLRequest): RlResponse {
        val result = storage.compute(req.id) { _, bucket ->
            val now = OffsetDateTime.now()
            when (bucket) {
                null -> UserBucket(now, maxTokens)
                else -> UserBucket(
                    now,
                    maxOf(0, RLUtils.refill(settings, bucket.tokenLeft, bucket.lastAccess, now) - 1)
                )
            }
        } ?: throw IllegalStateException("Unexpected null as bucket")

        return RlResponse(result.tokenLeft > 0)
    }
}
