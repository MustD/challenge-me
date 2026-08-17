package other.moneyTransfer

import java.math.BigDecimal
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.uuid.Uuid

class TransactionService {
    private val storage = ConcurrentHashMap<Uuid, BigDecimal>()

    fun createAccount(accountId: Uuid, initialAmount: BigDecimal) = runCatching<Boolean> {
        if (storage.putIfAbsent(accountId, initialAmount) != null) {
            throw IllegalStateException("already exist")
        }
        true
    }

    fun readAccount(accountId: Uuid) = runCatching<BigDecimal> {
        storage.getOrElse(accountId) { throw IllegalStateException("404") }
    }

    fun deposit(accountId: Uuid, amount: BigDecimal) = runCatching<BigDecimal> {
        storage.compute(accountId) { _, value ->
            value?.let { it + amount }
        } ?: throw IllegalStateException("404")
    }

    fun withdraw(accountId: Uuid, amount: BigDecimal) = runCatching<BigDecimal> {
        storage.compute(accountId) { _, value ->
            value?.let {
                if (value - amount < 0.toBigDecimal()) throw IllegalStateException("not enough")
                value - amount
            }
        } ?: throw IllegalStateException("404")
    }


    private val locks = ConcurrentHashMap<Uuid, ReentrantLock>()
    private fun lockFor(id: Uuid) = locks.computeIfAbsent(id) { ReentrantLock() }

    fun transfer(fromAccountId: Uuid, toAccountId: Uuid, amount: BigDecimal) = runCatching<Unit> {
        if (fromAccountId == toAccountId) throw IllegalArgumentException("unable to transfer from/to same account")
        val locks = listOf(fromAccountId, toAccountId).sorted().map { lockFor(it) }.onEach { it.lock() }

        runCatching {
            withdraw(fromAccountId, amount).getOrThrow()
            deposit(toAccountId, amount).getOrElse {
                deposit(fromAccountId, amount)
                throw it
            }
        }.also {
            locks.forEach { it.unlock() }
        }.getOrThrow()
    }

}
