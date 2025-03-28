package io.hhplus.tdd.point.model

import io.hhplus.tdd.point.domain.PointHistory
import io.hhplus.tdd.point.domain.TransactionType

/**
 * @author Doha Kim
 */
object PointHistoryTestFixture {
    private var idCounter = 1L
    private const val DEFAULT_AMOUNT = 1_000L

    fun getChargePointHistories(
        id: Long? = null,
        userId: Long,
        amount: Long = DEFAULT_AMOUNT,
    ): List<PointHistory> {
        var currentId = id ?: idCounter

        val histories = listOf(
            charge(currentId++, userId, amount),
            charge(currentId++, userId, amount),
        )

        if(id == null){
            idCounter = currentId
        }

        return histories
    }

    fun getChargePointHistory(
        id: Long? = null,
        userId: Long,
        amount: Long = DEFAULT_AMOUNT,
    ): List<PointHistory> {
        var currentId = id ?: idCounter

        val history = listOf(
            charge(currentId++, userId, amount),
        )

        if(id == null){
            idCounter = currentId
        }

        return history
    }

    fun getUsePointHistory(
        id: Long? = null,
        userId: Long,
        amount: Long = DEFAULT_AMOUNT,
    ): List<PointHistory> {
        var currentId = id ?: idCounter

        val history = listOf(
            use(currentId++, userId, amount),
        )

        if(id == null) {
            idCounter = currentId
        }

        return history
    }

    fun charge(
        id: Long = idCounter++,
        userId: Long,
        amount: Long = DEFAULT_AMOUNT,
        updateMillis: Long = System.currentTimeMillis()
    ) = PointHistory(
        id = id,
        userId = userId,
        type = TransactionType.CHARGE,
        amount = amount,
        timeMillis = updateMillis,
    )

    fun use(
        id: Long = idCounter++,
        userId: Long,
        amount: Long = DEFAULT_AMOUNT,
        timeMillis: Long = System.currentTimeMillis()
    ) = PointHistory(
        id = id,
        userId = userId,
        type = TransactionType.USE,
        amount = amount,
        timeMillis = timeMillis,
    )
}
