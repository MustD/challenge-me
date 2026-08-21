package cases.rate_limiter

import java.time.OffsetDateTime
import kotlin.time.Duration.Companion.minutes

internal object RLUtils {

    fun calculateMaxTokens(settings: RLSettings) = (1.minutes / settings.duration * settings.limit).toInt()

    fun refill(settings: RLSettings, currentTokens: Int, from: OffsetDateTime, to: OffsetDateTime): Int {
        val tokensToAdd =
            ((to.toEpochSecond() - from.toEpochSecond()) / settings.duration.inWholeSeconds) * settings.limit

        return minOf(
            calculateMaxTokens(settings),
            currentTokens + tokensToAdd.toInt()
        ) //todo: handle long -> int conversion risks
    }
}
