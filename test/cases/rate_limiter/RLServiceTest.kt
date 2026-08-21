package cases.rate_limiter

import org.junit.jupiter.api.Test
import kotlin.test.expect
import kotlin.test.fail
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.Uuid

class RLServiceTest {

    @Test
    fun validateRateSmoke() {
        val givenService = RLService(RLSettings(1.minutes, 3))
        val givenUserId = Uuid.generateV4()
        val givenRequest = RLRequest(givenUserId)

        runCatching {
            (1..10).map { givenService.validateRate(givenRequest) }
        }.onFailure {
            fail("Unexpected smoke result: $it")
        }.map {
            expect(true, "Unexpected limit on first request") { it.first().pass }
            expect(false, "Unexpected pass on last request") { it.last().pass }
        }


    }

}
