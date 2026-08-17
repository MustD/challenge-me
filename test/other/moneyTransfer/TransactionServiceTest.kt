package other.moneyTransfer

import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class TransactionServiceTest {

    @Test
    fun createAccountSuccess() {
        val givenService = TransactionService()
        val givenId = Uuid.generateV4()

        val result = givenService.createAccount(givenId, 0.toBigDecimal())

        result.getOrThrow().let(::assertTrue)
    }

    @Test
    fun createAccountAlreadyExist() {
        val givenService = TransactionService()
        val givenId = Uuid.generateV4()

        givenService.createAccount(givenId, 0.toBigDecimal())
        val result = givenService.createAccount(givenId, 0.toBigDecimal())

        assertThrows<IllegalStateException> { result.getOrThrow() }
    }

    @Test
    fun depositSuccess() {
        val service = TransactionService()
        val givenId = Uuid.generateV4().also { service.createAccount(it, 0.toBigDecimal()) }

        val result = service.deposit(givenId, 100.toBigDecimal())
        assertEquals(100.toBigDecimal(), result.getOrThrow(), "wrong")
    }

    @Test
    fun withdrawSuccess() {
        val service = TransactionService()
        val givenId = Uuid.generateV4().also { service.createAccount(it, 120.toBigDecimal()) }

        val result = service.withdraw(givenId, 100.toBigDecimal())
        assertEquals(20.toBigDecimal(), result.getOrThrow(), "wrong")
    }

    @Test
    fun withdrawNonEnoughBalance() {
        val service = TransactionService()
        val givenId = Uuid.generateV4().also { service.createAccount(it, 120.toBigDecimal()) }

        val result = service.withdraw(givenId, 200.toBigDecimal())
        assertThrows<IllegalStateException> { result.getOrThrow() }
    }

    @Test
    fun transferSuccess() {
        val service = TransactionService()
        val givenFrom = Uuid.generateV4().also { service.createAccount(it, 120.toBigDecimal()) }
        val givenTo = Uuid.generateV4().also { service.createAccount(it, 100.toBigDecimal()) }

        val result = service.transfer(givenFrom, givenTo, 60.toBigDecimal())

        assertDoesNotThrow { result.getOrThrow() }
    }

    @Test
    fun depositConcurrent() = runTest {
        val service = TransactionService()
        val givenUser = Uuid.generateV4()
        service.createAccount(givenUser, 0.toBigDecimal())

        val parallelism = 1000
        val jobs = (1..parallelism).map {
            launch { service.deposit(givenUser, 100.toBigDecimal()) }
        }

        jobs.joinAll()
        val result = service.readAccount(givenUser)

        assertEquals((100 * parallelism).toBigDecimal(), result.getOrThrow())
    }


}
