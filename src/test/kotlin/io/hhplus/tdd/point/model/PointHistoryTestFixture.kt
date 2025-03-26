package io.hhplus.tdd.point.model

import io.hhplus.tdd.point.PointHistory
import io.hhplus.tdd.point.TransactionType

/**
 * @author Doha Kim
 */
object PointHistoryTestFixture {
    private var idCounter = 1L
    private const val DEFAULT_AMOUNT = 1_000L

    fun charge(
        id: Long = idCounter++,
        userId: Long,
        amount: Long = DEFAULT_AMOUNT,
    ) = PointHistory(
        id = id,
        userId = userId,
        type = TransactionType.CHARGE,
        amount = amount,
        timeMillis = System.currentTimeMillis(),
    )

    fun use(
        id: Long = idCounter++,
        userId: Long,
        amount: Long = DEFAULT_AMOUNT,
    ) = PointHistory(
        id = id,
        userId = userId,
        type = TransactionType.USE,
        amount = amount,
        timeMillis = System.currentTimeMillis(),
    )
}
