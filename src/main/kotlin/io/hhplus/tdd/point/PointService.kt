package io.hhplus.tdd.point

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import io.hhplus.tdd.point.domain.PointHistory
import io.hhplus.tdd.point.domain.TransactionType
import io.hhplus.tdd.point.domain.UserPoint
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * @author Doha Kim
 */
@Service
class PointService(
    private val userPointTable: UserPointTable,
    private val pointHistoryTable: PointHistoryTable,
) {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun getUserPoint(id: Long): UserPoint {
        return userPointTable.selectById(id)
    }

    fun getUserPointHistories(id: Long): List<PointHistory> {
        return pointHistoryTable.selectAllByUserId(id)
    }

    fun chargeUserPoint(id: Long, amount: Long): UserPoint {
        val userPoint = getUserPoint(id)
        val updatedUserPoint = userPoint.charge(amount)

        pointHistoryTable.insert(
            id = userPoint.id,
            amount = amount,
            transactionType = TransactionType.CHARGE,
            updateMillis = userPoint.updateMillis,
        )

        return userPointTable.insertOrUpdate(id, updatedUserPoint.point)
    }

    fun useUserPoint(id: Long, amount: Long): UserPoint {
        val userPoint = getUserPoint(id)
        val updatedUserPoint = userPoint.use(amount)

        pointHistoryTable.insert(
            id = userPoint.id,
            amount = amount,
            transactionType = TransactionType.USE,
            updateMillis = userPoint.updateMillis,
        )

        return userPointTable.insertOrUpdate(id, updatedUserPoint.point)
    }
}
