package cases.rate_limiter

import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import kotlin.test.expect
import kotlin.time.Duration.Companion.seconds

class RLUtilsTest {
    @Test
    fun calculateMaxTokens() {
        val givenSettings = RLSettings(1.seconds, 10)
        runCatching { RLUtils.calculateMaxTokens(givenSettings) }.map {
            expect(600) { it }
        }.getOrThrow()

    }

    @Test
    fun refill() {
        val givenSettings = RLSettings(1.seconds, 10)
        val givenPeriod = 2
        val givenFrom = OffsetDateTime.now().minusMinutes(1)
        val givenTo = givenFrom.plusSeconds(givenPeriod.toLong())
        val current = 0

        runCatching {
            RLUtils.refill(
                givenSettings,
                currentTokens = current,
                from = givenFrom,
                to = givenTo
            )
        }.map {
            expect(givenSettings.limit * givenPeriod) { it }
        }.getOrThrow()
    }

}
