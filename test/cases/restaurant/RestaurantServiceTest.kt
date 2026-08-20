package cases.restaurant

import java.time.OffsetDateTime
import kotlin.test.Test
import kotlin.test.expect
import kotlin.test.fail

class RestaurantServiceTest {

    @Test
    fun addTableSmoke() {
        val givenService = RtService()

        val actual = runCatching {
            givenService.addTable(10)
            givenService.addTable(10)
        }
        actual.onFailure {
            fail("Err: $it")
        }
    }

    @Test
    fun addTablePos() {
        val givenService = RtService()

        givenService.addTable(10)
        givenService.addTable(10)

        val actual = runCatching { givenService.tableList() }
        actual.onFailure {
            fail("Err: $it")
        }.map {
            expect(true, "Err: table capacity") { it.all { table -> table.capacity == 10 } }
        }
    }


    //todo: check if rt has no tables

    @Test
    fun bookSmoke() {
        val givenService = RtService()
        val giveRtBookRequest = RtBooking(
            capacity = 10,
            name = "John",
            startTime = OffsetDateTime.now().plusDays(1),
            endTime = OffsetDateTime.now().plusDays(2)
        )
        givenService.addTable(10)


        val actual = runCatching { givenService.book(giveRtBookRequest) }
        actual.onFailure {
            fail("Err: $it")
        }


    }

    @Test
    fun bookCancelSmoke() {
        val givenService = RtService()
        val giveRtBookRequest = RtBooking(
            capacity = 10,
            name = "John",
            startTime = OffsetDateTime.now().plusDays(1),
            endTime = OffsetDateTime.now().plusDays(2)
        )
        givenService.addTable(10)
        val givenBookingId = givenService.book(giveRtBookRequest) ?: throw RuntimeException("Unexpected fail")

        val actual = runCatching { givenService.cancel(givenBookingId) }
        actual.onFailure {
//            fail("Err: $it")
        }


    }


}
